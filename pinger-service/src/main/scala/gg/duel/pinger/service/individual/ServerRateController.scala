package gg.duel.pinger.service.individual

import akka.actor.ActorDSL._
import akka.actor.Cancellable
import gg.duel.pinger.data.Server
import gg.duel.pinger.service.PingPongProcessor.Ping
import gg.duel.pinger.service.individual.ServerMonitor._

import scala.concurrent.duration._

/**
 * Send pings out at intervals dependent on the current server state.
 * Either: 3 seconds (Online-Active/Initialising), or 30 seconds (Offline/Online-Empty)
 * We don't want to get too much crappy data coming in.
 */
class ServerRateController(server: Server) extends Act {

  import scala.concurrent.ExecutionContext.Implicits.global

  case object Start

  case object Refresh
  
  whenStarting {
    self ! Start
  }

  def initialised(state: ServerState, schedule: Cancellable): Receive = {

    def changeRate(rate: FiniteDuration) {
      schedule.cancel()
      become {
        initialised(
          state = state,
          schedule = context.system.scheduler.schedule(rate, rate, self, Refresh)
        )
      }
    }

    {

      case Refresh =>
        context.parent ! Ping(server)

      case ServerStateChanged(`server`, Online(Active)) =>
        changeRate(3.seconds)

      case ServerStateChanged(`server`, Offline | Online(Empty)) =>
        changeRate(30.seconds)

    }
  }

  become {
    case Start =>
      become {
        initialised (
          state = Offline,
          schedule = context.system.scheduler.schedule(0.seconds, 3.seconds, self, Refresh)
        )
      }
  }

}
