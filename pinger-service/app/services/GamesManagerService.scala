package services

import javax.inject.{Inject, Singleton}

import akka.agent.Agent
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import models.games.GamesContainer
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class GamesManagerService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, applicationLifecycle: ApplicationLifecycle) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val gamesT = TableQuery[GamesTable]

  val gamesA = Agent(GamesContainer.empty)

  val gamesLoadedF = dbConfig.db.run(gamesT.result).map(_.map {
    case (id, json) => gamesA.alter(_.withGame(id, Json.parse(json)))
  }).flatMap(fs => Future.sequence(fs))

  def games: GamesContainer = gamesA.get()

  def addDuel(duel: SimpleCompletedDuel): Unit = {
    dbConfig.db.run(gamesT.insertOrUpdate(duel.startTimeText, duel.toPrettyJson)).onComplete {
      case Success(good) => Logger.info(s"Saved duel ${duel.startTimeText} successfully.")
      case Failure(reason) => Logger.error(s"Failed to save duel ${duel.toJson}", reason)
    }
    gamesA.alter(_.withGame(duel.startTimeText, Json.parse(duel.toPrettyJson)))
  }

  def addCtf(ctf: SimpleCompletedCTF): Unit = {
    dbConfig.db.run(gamesT.insertOrUpdate(ctf.startTimeText, ctf.toPrettyJson)).onComplete {
      case Success(good) => Logger.info(s"Saved ctf ${ctf.startTimeText} successfully.")
      case Failure(reason) => Logger.error(s"Failed to save ctf ${ctf.toJson}", reason)
    }
    gamesA.alter(_.withGame(ctf.startTimeText, Json.parse(ctf.toPrettyJson)))
  }

}

