package us.woop.pinger.service

import akka.actor.Props
import akka.actor.ActorDSL._
import us.woop.pinger.client.PingPongProcessor.Server

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
