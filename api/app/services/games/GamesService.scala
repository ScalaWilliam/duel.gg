package services.games

import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import gg.duel.enricher.lookup.BasicLookingUp
import gg.duel.query.QueryableGame
import lib.{GeoLookup, JsonGameToSimpleGame, OgroDemoParser, SucceedOnceFuture}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import services.ClansService
import services.demos.{DemoCollection, DemosListing}
import scala.async.Async
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions


@Singleton
class GamesService @Inject()(clansService: ClansService, demoCollectorModule: DemoCollection,
                             applicationLifecycle: ApplicationLifecycle, configuration: Configuration,
                             wSClient: WSClient)
                            (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  implicit val scheduler = actorSystem.scheduler

  implicit val am = ActorMaterializer()

  implicit class sgToEvents(simpleGame: QueryableGame) {

    def reEnrich: QueryableGame = {
      sseToGameOption.apply(json = simpleGame.enhancedJson).getOrElse(simpleGame)
    }
  }

  val gamesAgt = Agent(Games.empty)

  def limitLoad: Option[Int] = {
    for {
      ll <- configuration.getBoolean("gg.duel.limit-game-load")
      if ll
      n <- configuration.getInt("gg.duel.limit-game-load-size")
    } yield n
  }

  def pingerPath = configuration.getString("gg.duel.api.pinger-service.url").getOrElse {
    throw new RuntimeException("Config option 'gg.duel.api.pinger-service.url' not set!")
  }

  def loadGamesFromDatabaseFuture = {

    val f = Async.async {
      Logger.info("Loading games...")
      val res = Async.await(wSClient.url(s"$pingerPath/games/all/").get())
      require(res.status == 200, s"Response status should be 200, got ${res.status}")
      val ctype = res.header("Content-Type")
      require(ctype.contains("text/plain"), s"Returned content should be plaintext, got $ctype")
      val lines = res.body.split("\n")
      Logger.info(s"Received file; ${lines.size} lines.")
      val parseLine = "([^\t]+)\t(.*)".r
      val flow = Source(() => lines.toIterator).collect { case parseLine(id, json) => json }
        .mapAsyncUnordered(8) { json =>
          Future(sseToGameOption(json = json).toList)
        }.mapConcat(identity).mapAsyncUnordered(2) { game =>
        val f = gamesAgt.alterOff(_.withNewGame(game))
        f.onFailure { case x => println(s" ==> $x") }
        f
      }

      Async.await(flow.runFold(gamesAgt.get().games.size) { case (n, _) => n + 1 })
    }

    f.onFailure { case reas: Throwable =>
      Logger.error(s"Loading games from database failed due to $reas. Trying again.", reas)

    }

    f
  }

  import concurrent.duration._

  val loadGamesFromDatabaseOnce = new SucceedOnceFuture(loadGamesFromDatabaseFuture)(_ => 5.seconds)

  def loadGamesFromDatabase = loadGamesFromDatabaseOnce.value

  loadGamesFromDatabaseOnce.finalValue.onSuccess { case result =>
    Logger.info(s"Loading $result games from database completed.")

  }
  lazy val enricher = new BasicLookingUp(
    game => for {server <- game.server; mode <- game.mode; map <- game.map; st <- game.startTime
                 demo <- demosAgt.get().lookupFromGame(
                   server = server,
                   mode = mode,
                   map = map,
                   atTime = st
                 )
    } yield demo,
    clanLookup = nickname => clansService.clans.collectFirst { case (id, clan) if clan.nicknameIsInClan(nickname) => id },
    countryLookup = GeoLookup.apply
  )

  def sseToGameOption = JsonGameToSimpleGame(enricher = enricher)

  val demosAgt = Agent(DemosListing.empty)


  val kr = demoCollectorModule.demoFetching.to(Sink.foreach { demosFetched =>
    demosAgt.alterOff(_ ++ demosFetched).map { _ =>
      gamesAgt.get().games.collect { case (id, game) if OgroDemoParser.servers.contains(game.server) && game.demo.isEmpty =>
        val reenriched = game.reEnrich
        game -> reenriched
      }.collect { case (g, r) if g != r => r }.foreach { ng => gamesAgt.send(_ + ng) }
    }
  }).run()

  applicationLifecycle.addStopHook(() => Future.successful {
    kr.cancel()
  })

  // push to:
  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[(Option[QueryableGame], Event)]
  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[(Option[QueryableGame], Event)]
  // and also the agent

  val keepAliveEveryTenSeconds = actorSystem.scheduler.schedule(5.seconds, 10.seconds) {
    val emptyEvent = Event(
      data = "",
      id = Option.empty,
      name = Option.empty
    )
    newGamesChan.push(None -> emptyEvent)
    liveGamesChan.push(None -> emptyEvent)
  }

}


object GamesService {

  implicit class sgToEvent(simpleGame: QueryableGame) {
    def toEvent = Event(
      name = Option(simpleGame.gameType),
      id = Option(simpleGame.id),
      data = simpleGame.enhancedJson
    )
  }

}