package modules

import java.time.{ZoneId, ZonedDateTime}
import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import gcc.enrichment.{DemoLookup, Enricher, PlayerLookup}
import gcc.game.GameReader
import gg.duel.SimpleGame
import org.joda.time.DateTime
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

import scala.async.Async
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Try, Failure, Success}

@Singleton
class GamesService @Inject()(dbConfigProvider: DatabaseConfigProvider, upstreamGames: UpstreamGames, clansService: ClansService, demoCollectorModule: DemoCollection,
                             applicationLifecycle: ApplicationLifecycle, configuration: Configuration)
                            (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val gamesT = TableQuery[GamesTable]
  implicit val am = ActorMaterializer()

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

  val gamesAgt = Agent(Games.empty)

  def allGamesQuery = {
    var q = gamesT.sortBy(_.id)
    if ( configuration.getBoolean("gg.duel.limit-game-load") == Some(true) ) {
      q = q.take(500)
    }
    q
  }

  val loadGamesFromDatabase = Async.async {
    Logger.info("Loading games from database...")
    Async.await {
      Source(dbConfig.db.stream(allGamesQuery.result)).mapAsyncUnordered(8) { case (id, json) =>
        Future(sseToGameOption(id = id, json = json).toList)
      }.mapConcat(identity).mapAsyncUnordered(2){
        game =>
          val f = gamesAgt.alterOff(_.withNewGame(game))
          f.onFailure{ case x => println(s"==> $x")}
          f
      }.runFold(0){case (n,_) => n+1}
    }
  }

  loadGamesFromDatabase.onComplete {
    case Success(result) =>
      Logger.info(s"Loading $result games from database completed.")
    case Failure(reas) =>
      Logger.error(s"Loading games from database failed due to $reas", reas)
  }

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = null

    override def lookupClanId(nickname: String, atTime: DateTime): String = {
      clansService.clans.collectFirst { case (id, clan) if clan.nicknameIsInClan(nickname) => id }.orNull
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

  lazy val demoLookup = new DemoLookup {
    override def lookupDemoUrl(server: String, mode: String, map: String, atTime: org.joda.time.DateTime): String = {
      demosAgt.get().lookupFromGame(
        server = server,
        mode = mode,
        map = map,
        atTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(atTime.toInstant.getMillis), ZoneId.of("UTC"))
      ).orNull
    }
  }
  lazy val gameReader = new GameReader()
  lazy val enricher = new Enricher(playerLookup, demoLookup)

  def sseToGameOption = JsonGameToSimpleGame(enricher = enricher, gameReader = gameReader)

  val demosAgt = Agent(DemosListing.empty)

  upstreamGames.newClient.createStream(sseToGameOption.createFlow.to(Sink.foreach { game => gamesAgt.sendOff(_.withNewGame(game)) }))


  val kr = demoCollectorModule.demoFetching.to(Sink.foreach { demosFetched =>
    demosAgt.alterOff(_ ++ demosFetched).map { _ =>
      gamesAgt.get().games.collect { case (id, game) if OgroDemoParser.servers.contains(game.server) && game.demo.isEmpty =>
        val reenriched = game.reEnrich
        game -> reenriched
      }.collect { case (g, r) if g != r => r }.foreach { ng => gamesAgt.send(_ + ng) }
    }
  }).run()

  applicationLifecycle.addStopHook(() => Future.successful{
    kr.cancel()
    xs.success(())
    xl.success(())
  })

  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]

  val xs = upstreamGames.newClient.createStream(sseToGameOption.createFlow.to(Sink.foreach(game => newGamesChan.push(game -> game.toEvent))))

  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]

  val xl = upstreamGames.liveGames.createStream(UpstreamGames.flw.mapConcat(sse =>
    sseToGameOption.apply(sse).toList.map(sg => sse -> sg)
  ).map { case (sse, sg) => sg -> sg.toEvent.copy(name = sse.eventType) }.to(Sink.foreach(event => liveGamesChan.push(event))))

}

class GamesTable(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}
