package us.woop.pinger.client

import akka.actor.ActorDSL._
import akka.pattern._
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import us.woop.pinger.MasterserverClient
import us.woop.pinger.client.data.MasterserverClientActor

/** Sends:
  * Servers
  * ServerGone
  * ServerAdded
  */
class MasterserverClientActor extends Act with ActorLogging  {

  import MasterserverClientActor._
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

