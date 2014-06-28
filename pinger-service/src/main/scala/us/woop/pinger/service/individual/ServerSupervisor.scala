package us.woop.pinger.service.individual

import akka.actor.ActorDSL._
import akka.actor.Props
import us.woop.pinger.data.Stuff.Server

class ServerSupervisor(server: Server) extends Act {
  whenStarting {
    context.actorOf(
      name = "monitor",
      props = Props(classOf[ServerMonitor], server)
    )

    context.actorOf(
      name = "rateController",
      props = Props(classOf[ServerRateController], server)
    )
  }
}
