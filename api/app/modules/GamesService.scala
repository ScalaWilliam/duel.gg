package modules

import java.time.{ZoneId, ZonedDateTime}
import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import gcc.enrichment.{DemoLookup, Enricher, PlayerLookup}
import gcc.game.GameReader
import gg.duel.SimpleGame
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions


@Singleton
class GamesService @Inject()(upstreamGames: UpstreamGames, clansService: ClansService, demoCollectorModule: DemoCollectorModule,
                         applicationLifecycle: ApplicationLifecycle)
                        (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  implicit class sgToEvent(simpleGame: SimpleGame) {
    def toEvent = Event(
      name = Option(simpleGame.gameType),
      id = Option(simpleGame.id),
      data = simpleGame.enhancedJson
    )
    def reEnrich: SimpleGame = {
      sseToGameOption.apply(id = simpleGame.id, json = simpleGame.enhancedJson).getOrElse(simpleGame)
    }
  }

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = null

    override def lookupClanId(nickname: String, atTime: DateTime): String = {
      clansService.clans.collectFirst{case (id, clan) if clan.nicknameIsInClan(nickname)=> id}.orNull
    }
  }

  case class Games
  (games: Map[String, SimpleGame]) {
    def withNewGame(simpleGame: SimpleGame): Games = {
      copy(games = games + (simpleGame.id -> simpleGame))
    }
    def +(simpleGame: SimpleGame): Games = withNewGame(simpleGame)
  }

  object Games {
    def empty: Games = Games(
      games = Map.empty
    )
  }

  val demoLookup = new DemoLookup {
    override def lookupDemoUrl(server: String, mode: String, map: String, atTime: org.joda.time.DateTime): String = {
      demosAgt.get().lookupFromGame(
        server = server,
        mode = mode,
        map = map,
        atTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(atTime.toInstant.getMillis), ZoneId.of("UTC"))
      ).orNull
    }
  }
  val gamesAgt = Agent(Games.empty)
  val gameReader = new GameReader()
  val enricher = new Enricher(playerLookup, demoLookup)

  def sseToGameOption = JsonGameToSimpleGame(enricher = enricher, gameReader = gameReader)

  val demosAgt = Agent(DemosListing.empty)

  val aln = upstreamGames.allAndNewClient.createStream(sseToGameOption.createFlow.to(Sink.foreach { game => gamesAgt.sendOff(_.withNewGame(game)) }))

  implicit val am = ActorMaterializer()
  val kr = demoCollectorModule.demoFetching.to(Sink.foreach{demosFetched =>
    demosAgt.alterOff(_ ++ demosFetched).map{_ =>
      gamesAgt.get().games.collect { case (id, game) if OgroDemoParser.servers.contains(game.server) && game.demo.isEmpty =>
        val reenriched = game.reEnrich
        game -> reenriched
      }.collect{case (g, r) if g != r => r}.foreach{ng => gamesAgt.send(_ + ng)}
    }
  }).run()

  applicationLifecycle.addStopHook(() => scala.concurrent.Future.successful(kr.cancel() -> xs.success(()) -> xl.success() -> aln.success()))

  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]

  val xs = upstreamGames.newClient.createStream(sseToGameOption.createFlow.to(Sink.foreach(game => newGamesChan.push(game -> game.toEvent))))

  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]

  val xl = upstreamGames.liveGames.createStream(upstreamGames.flw.mapConcat(sse =>
    sseToGameOption.apply(sse).toList.map(sg => sse -> sg)
  ).map{case (sse, sg) => sg -> sg.toEvent.copy(name = sse.eventType)}.to(Sink.foreach(event => liveGamesChan.push(event))))

}