package controllers

import javax.inject._

import gg.duel.pinger.data.Server
import models.games.GamesManager
import models.pinger.PingerService
import models.servers.ServerManager
import play.api.libs.EventSource
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()
(gamesManager: GamesManager,
 pingerService: PingerService,
 serverProvider: ServerManager)
(implicit executionContext: ExecutionContext) extends Controller {

  def servers = Action {
    Ok(serverProvider.servers)
  }

  def putServer(serverId: String) = Action {
    serverProvider.addServer(serverId, Server.fromAddress(serverId))
    Ok(serverId)
  }

  def deleteServer(serverId: String) = Action {
    serverProvider.deleteServer(serverId)
    Ok(serverId)
  }

  def games(from: Option[String], to: Option[String]) = Action {

    def ctfsM = gamesManager.games.ctfs.filter(g => from.isEmpty || from.exists(time => g._1 >= time)).mapValues(writesSCC.writes)
    def duelsM = gamesManager.games.duels.filter(g => to.isEmpty || to.exists(time => g._1 <= time)).mapValues(writesSCD.writes)
    def initial = (ctfsM ++ duelsM).toList.sortBy(_._1).map(_._2)

    Ok(JsArray(initial))
  }

  def gamesStream(from: Option[String]) = Action {
    implicit req =>
      from match {
        case None =>
          Ok.feed(pingerService.enumerator &> EventSource()).as("text/event-stream")
        case Some(time) =>
          def ctfsM = gamesManager.games.ctfs.filter(_._1 >= time).mapValues(_.toPrettyJson)
          def duelsM = gamesManager.games.duels.filter(_._1 >= time).mapValues(_.toPrettyJson)
          def initial = (ctfsM ++ duelsM).toList.sortBy(_._1).map(_._2)
          def combo = Enumerator.enumerate(initial) andThen pingerService.enumerator
          Ok.feed(combo &> EventSource()).as("text/event-stream")
      }
  }

  def currentStatus = Action {
//    val cs = new CustomSerializer[Server](fmt => (PartialFunction.empty,
//      { case s: Server => JString(s.getAddress) }))
//    implicit val fmts = DefaultFormats + cs
//    import org.json4s.jackson.Serialization._
//    Ok(writePretty(pingerService.ourState.get()))
    Ok(pingerService.ourState.get().toString())
  }

}