package controllers

import javax.inject._

import modules.ServerManager
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import services._

import scala.async.Async
import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()
(gamesManager: GamesManagerService,
 pingerService: PingerService,
 serverProvider: ServerManager,
 journallingService: JournallingService,
 rabbitMQSinkService: RabbitMQSinkService)
(implicit executionContext: ExecutionContext) extends Controller {

  def index = Action.async {
    Async.async {
      Async.await(gamesManager.gamesLoadedF)
      Ok(views.html.index())
    }
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
  def getAllGames = Action {
    Ok.sendFile(content = gamesManager.f, inline = true)
  }

}