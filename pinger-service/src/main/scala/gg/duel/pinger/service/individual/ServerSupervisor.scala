package gg.duel.pinger.service.individual

import akka.actor.ActorDSL._
import akka.actor.{ActorRef, Props}
import gg.duel.pinger.data.Server

class ServerSupervisor(server: Server) extends Act {

  case class Dependencies(serverMonitor: ActorRef, serverRateController: ActorRef) {
    val items = Vector(serverMonitor, serverRateController)
    def !(msg: Any) = items foreach (_ ! msg)
  }

  whenStarting {
    self ! Dependencies(
      serverMonitor = context.actorOf(
        name = "monitor",
        props = Props(classOf[ServerMonitor], server)
      ),
      serverRateController = context.actorOf(
        name = "rateController",
        props = Props(classOf[ServerRateController], server)
      )
    )
  }

  become {
    case dependencies: Dependencies =>
      become {
        case any if dependencies.items contains sender() =>
          context.parent ! any
        case other =>
          dependencies ! other
      }
  }

}
