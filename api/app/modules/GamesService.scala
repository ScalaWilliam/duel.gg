package modules

import java.io._
import java.time.{ZoneId, ZonedDateTime}
import javax.inject._

import akka.actor.{Scheduler, ActorSystem}
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import gcc.enrichment.{DemoLookup, Enricher, PlayerLookup}
import gcc.game.GameReader
import gg.duel.SimpleGame
import io.scalac.amqp.{Delivery, Connection}
import lib.SucceedOnceFuture
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


case class Games
(games: Map[String, SimpleGame]) {
  me =>
  def withNewGame(simpleGame: SimpleGame): Games = {
    copy(games = games + (simpleGame.id -> simpleGame))
  }

  def +(simpleGame: SimpleGame): Games = withNewGame(simpleGame)

  def ++(games: Games): Games = Games(
    games = me.games ++ games.games
  )
}

object Games {
  def empty: Games = Games(
    games = Map.empty
  )
}

@Singleton
class GamesService @Inject()(dbConfigProvider: DatabaseConfigProvider, clansService: ClansService, demoCollectorModule: DemoCollection,
                             applicationLifecycle: ApplicationLifecycle, configuration: Configuration)
                            (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  implicit val scheduler = actorSystem.scheduler

  applicationLifecycle.addStopHook(() => Future.successful(dbConfig.db.close()))

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
    (configuration.getBoolean("gg.duel.limit-game-load"),
      configuration.getInt("gg.duel.limit-game-load-size")) match {
      case (Some(true), Some(number)) =>
        q = q.sortBy(_.id.desc).take(number)
      case _ =>

    }
    q
  }

  def loadGamesFromDatabaseFuture = {
    val f = Async.async {

      var qr = allGamesQuery.result

      Logger.info("Loading games from database...")
      Async.await {
        Source(dbConfig.db.stream(qr)).mapAsyncUnordered(8) { case (id, json) =>
          Future(sseToGameOption(id = id, json = json).toList)
        }.mapConcat(identity).mapAsyncUnordered(2) {
          game =>
            val f = gamesAgt.alterOff(_.withNewGame(game))
            f.onFailure { case x => println(s"==> $x") }
            f
        }.runFold(gamesAgt.get().games.size) { case (n, _) => n + 1 }
      }
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

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = null

    override def lookupClanId(nickname: String, atTime: DateTime): String = {
      clansService.clans.collectFirst { case (id, clan) if clan.nicknameIsInClan(nickname) => id }.orNull
    }
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
  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]
  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[(SimpleGame, Event)]
  // and also the agent


}

class GamesTable(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}
