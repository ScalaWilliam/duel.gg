package us.woop.pinger
import akka.actor.ActorDSL._
import MasterserverClientActor._
import akka.pattern._
import akka.actor.ActorLogging
import akka.event.LoggingReceive

/** Sends:
  * Servers
  * ServerGone
  * ServerAdded
  */
class MasterserverClientActor extends Act with ActorLogging  {

  var previousServers = Set[(String, Int)]()
  import scala.concurrent.ExecutionContext.Implicits.global
  become(LoggingReceive{
    case RefreshServerList =>
      import scala.concurrent.future
      future {
        val servers = MasterserverClient.getServers(MasterserverClient.sauerMasterserver)
        log.info("Received {} servers", servers.size)
        self ! MasterServers(servers)
      } pipeTo self
    case masterServers @ MasterServers(servers) =>
      val newServers = servers.diff(previousServers)
      val goneServers = previousServers.diff(servers)
      for {server <- goneServers} {
        context.parent ! ServerGone(server)
      }
      // if we've already been running for a while, send Servers without ServerAdded.
      for {
        server <- newServers
        if previousServers.nonEmpty
      } context.parent ! ServerAdded(server)
      previousServers = servers
      context.parent ! masterServers
  })
  
}

object MasterserverClientActor {

  case object RefreshServerList

  case class MasterServers(servers: Set[(String, Int)])

  case class ServerGone(server: (String, Int))

  case class ServerAdded(server: (String, Int))

}