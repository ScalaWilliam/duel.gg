package controllers

import javax.inject._
import modules.{GamesManager, ServerManager}
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import services.{LoadJournalledIntoCore, PingerService, ReadJournalledService, JournallingService}

import scala.async.Async
import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()
(gamesManager: GamesManager,
 pingerService: PingerService,
 serverProvider: ServerManager,
 journallingService: JournallingService,
 readJournalledService: ReadJournalledService,
  loadJournalledIntoCore: LoadJournalledIntoCore)
(implicit executionContext: ExecutionContext) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  private def allGamesEnum = Enumerator.enumerate(
    traversable = gamesManager.games.asCombined.games.sortBy(_.fold(_.startTime, _.startTime)).map(_.fold(scd =>
      Event(
        id = Option(scd.startTimeText),
        name = Option("duel"),
        data = scd.toJson), scc =>
      Event(
        id = Option(scc.startTimeText),
        name = Option("ctf"),
        data = scc.toJson
      )))
  )

  def allGames = Action {
    implicit req =>
      val endToken = Event(data = "end", id = Option("end"), name = Option("end"))
      Ok.feed(content = allGamesEnum.andThen(Enumerator.apply(endToken))).as("text/event-stream")
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

  def allGamesAndNew = Action {
    implicit req =>
      Ok.feed(
        content = allGamesEnum.andThen(pingerService.enumerator)
      ).as("text/event-stream")
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

  def recent = Action {
    Ok(gamesManager.games.asCombined.latest(50).reverse)
  }

  def currentStatus = Action {
    Ok(pingerService.ourState.get().toString)
  }

}