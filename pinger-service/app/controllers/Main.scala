package controllers

import javax.inject._

import gg.duel.pinger.data.Server
import models.games.{Ctfs, Duels, GamesIndex, GamesManager}
import models.pinger.PingerService
import models.servers.ServerManager
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()
(gamesManager: GamesManager,
 pingerService: PingerService,
 serverProvider: ServerManager)
(implicit executionContext: ExecutionContext) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  private def allGamesEnum = Enumerator.enumerate(
    traversable = gamesManager.games.asCombined.games.map(_.fold(scd =>
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
      Ok.feed(content = allGamesEnum).as("text/event-stream")
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

  def putServer(serverId: String) = Action {
    serverProvider.addServer(serverId, Server.fromAddress(serverId))
    Ok(serverId)
  }

  def deleteServer(serverId: String) = Action {
    serverProvider.deleteServer(serverId)
    Ok(serverId)
  }

  def currentStatus = Action {
    Ok(pingerService.ourState.get().toString)
  }

}