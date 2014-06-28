package us.woop.pinger.service

import akka.actor.ActorDSL._
import us.woop.pinger.service.ServerMonitor.ServerStateChanged
import concurrent.duration._
import us.woop.pinger.PongParser.GetServerInfoReply
import us.woop.pinger.client.PingPongProcessor.{ReceivedBytes, BadHash, Server}
import us.woop.pinger.data.actor.IndividualServerProcessor._

object ServerMonitor {
  case class ServerStateChanged(server: Server, serverState: ServerState)
}

/**
 * Determine a server's status - whether it's offline, online-empty, online-active.
 * @param server
 */
class ServerMonitor(server: Server) extends Act {

  import scala.concurrent.ExecutionContext.Implicits.global

  case object CheckIfGone

  def alert(state: ServerState) {
    context.system.eventStream.publish(ServerStateChanged(server, state))
  }

  whenStarting {
    context.system.scheduler.schedule(3.seconds, 3.seconds, self, CheckIfGone)
    context.system.eventStream.subscribe(self, classOf[BadHash])
    context.system.eventStream.subscribe(self, classOf[ReceivedBytes])
  }


  become {
    uninitialised()
  }

  def initialiseState(state: ServerState) {
    alert(state)
    become(initialised(state))
  }

  def uninitialised(offlineChecksFailed: Int = 0): Receive = {

    // After 5 no-messages, make the server offline. Ain't getting no pongs.
    case CheckIfGone if offlineChecksFailed == 5 =>
      alert(Offline)
      become(initialised(Offline))

    case CheckIfGone =>
      become(uninitialised(offlineChecksFailed + 1))

    case m @ ReceivedBytes(`server`, _, GetServerInfoReply(serverinforeply)) if serverinforeply.clients > 0 =>
      initialiseState { Online(Active) }

    case m @ ReceivedBytes(`server`, _, GetServerInfoReply(serverinforeply)) =>
      initialiseState { Online(Empty) }

  }

  def initialised(currentState: ServerState, lastReceived: Long = System.currentTimeMillis): Receive = {

    def updateState(newState: ServerState = currentState) {
      if ( newState != currentState ) {
        alert(newState)
      }
      become(initialised(newState))
    }

    {

      case CheckIfGone if currentState != Offline && (lastReceived - System.currentTimeMillis).millis > 1.minute =>
        updateState { Offline }

      case ReceivedBytes(`server`, _, GetServerInfoReply(serverinforeply)) if serverinforeply.clients > 0 =>
        updateState { Online(Active) }

      case ReceivedBytes(`server`, _, GetServerInfoReply(serverinforeply)) =>
        updateState { Online(Empty) }

      case BadHash(`server`, _, _, _, _) =>
        updateState()

    }
  }
}

