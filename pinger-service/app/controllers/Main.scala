package controllers

import javax.inject._

import gg.duel.pinger.data.Server
import models.games.{Ctfs, Duels, GamesIndex, GamesManager}
import models.pinger.PingerService
import models.servers.ServerManager
import play.api.libs.EventSource
import play.api.libs.iteratee.Iteratee
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

  def servers = Action {
    Ok(serverProvider.servers)
  }

  def range(from: Long, to: Long) = Action {
    Ok(gamesManager.games.asCombined.range(from, to))
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

  def gamesIndex = Action {
    Ok(GamesIndex(
      ctfs = gamesManager.games.ctfs.valuesIterator.map(_.startTime).toList.sorted.map(_.toString),
      duels = gamesManager.games.duels.valuesIterator.map(_.startTime).toList.sorted.map(_.toString)
    ))
  }

  def duels(from: Long, to: Long) = Action {
    Ok(Duels(
      gamesManager.games.duels.valuesIterator.filter {
        game => game.startTime >= from && game.startTime <= to
      }.toList.sortBy(_.startTime)
    ))
  }

  def ctfs(from: Long, to: Long) = Action {
    Ok(Ctfs(
      gamesManager.games.ctfs.valuesIterator.filter {
        game => game.startTime >= from && game.startTime <= to
      }.toList.sortBy(_.startTime)
    ))
  }

  def gamesStreamSse = Action {
    implicit req =>
      Ok.feed(pingerService.enumerator &> EventSource()).as("text/event-stream")
  }

  def gamesStreamWs = WebSocket.using[String] { request =>
    (Iteratee.isEmpty, pingerService.enumerator)
  }

  def currentStatus = Action {
    Ok(pingerService.ourState.get().toString)
  }

}