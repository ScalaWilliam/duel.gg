package controllers

import javax.inject._

import modules.{GamesManager, ServerManager}
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import services._

import scala.async.Async
import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()
(gamesManager: GamesManager,
 pingerService: PingerService,
 serverProvider: ServerManager,
 journallingService: JournallingService,
 readJournalledService: ReadJournalledService,
 loadJournalledIntoCore: LoadJournalledIntoCore,
 serveLiveSauerBytesService: ServeLiveSauerBytesService)
(implicit executionContext: ExecutionContext) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  private def allGamesEnum = Async.async {
    Async.await(gamesManager.gamesLoadedF)
    Enumerator.enumerate(
      traversable = gamesManager.games.games.toList.sortBy(_._1).map { case (id, gameJson) =>
        val gameType = if ( (gameJson \ "mode").asOpt[String].get.contains("ctf") ) "ctf" else "duel"
        Event(
          id = Option(id),
          name = Option(gameType),
          data = gameJson.toString
        )
      }
    )
  }

  def allGames = Action.async {
    implicit req =>
      Async.async {
        val endToken = Event(data = "end", id = Option("end"), name = Option("end"))
        Ok.feed(content = Async.await(allGamesEnum).andThen(Enumerator.apply(endToken))).as("text/event-stream")
      }
  }

  def readParsed = Action.async {
    Async.async {
      Ok {
        JsArray(Async.await(readJournalledService.parsedGamesFuture)
          .map(_.fold(_.toJson, _.toJson))
          .map(Json.parse))
      }
    }
  }

  def allGamesAndNew = Action.async {
    implicit req =>
      Async.async {
        Ok.feed(
          content = Async.await(allGamesEnum).andThen(pingerService.enumerator)
        ).as("text/event-stream")
      }
  }

  def liveGames = Action {
    Ok.feed(content = pingerService.liveGameEnumerator).as("text/event-stream")
  }

  def newGames = Action {
    implicit req =>
      Ok.feed(
        content = pingerService.enumerator
      ).as("text/event-stream")
  }

  def servers = Action {
    Ok(serverProvider.servers)
  }

  def recent = Action.async {
    Async.async {
      Async.await(gamesManager.gamesLoadedF)
      Ok(JsArray(gamesManager.games.latest(50).map(_._2)))
    }
  }

  def currentStatus = Action {
    Ok(pingerService.ourState.get().toString)
  }

}