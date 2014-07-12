package us.woop.pinger.service.individual

import akka.actor.ActorDSL._
import us.woop.pinger.PongParser.GetServerInfoReply
import us.woop.pinger.data.ParsedPongs.ServerInfoReply
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingPongProcessor.{BadHash, ReceivedBytes}
import us.woop.pinger.service.RawToExtracted.ExtractedMessage
import us.woop.pinger.service.individual.ServerMonitor._
import scala.concurrent.duration._

object ServerMonitor {

  sealed trait ServerState
  case class Online(gameStatus: GameStatus) extends ServerState
  case object Offline extends ServerState
  case object Initialising extends ServerState

  sealed trait GameStatus
  case object Empty extends GameStatus
  case object Active extends GameStatus
  case class ServerStateChanged(server: Server, serverState: ServerState)

}

/**
 * Determine a server's status - whether it's offline, online-empty, online-active.
 */
class ServerMonitor(server: Server) extends Act {

  import scala.concurrent.ExecutionContext.Implicits.global

  case object CheckIfGone

  def alert(state: ServerState) {
    context.parent ! ServerStateChanged(server, state)
  }

  whenStarting {
    context.system.scheduler.schedule(3.seconds, 3.seconds, self, CheckIfGone)
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

    case m @ ExtractedMessage(`server`, _, serverinforeply: ServerInfoReply) if serverinforeply.clients > 0 =>
      initialiseState { Online(Active) }

    case m @ ExtractedMessage(`server`, _, serverinforeply: ServerInfoReply) =>
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

