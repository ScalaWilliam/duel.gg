package us.woop.pinger.actors

import akka.actor.ActorLogging
import akka.actor.ActorDSL._
import us.woop.pinger.data.actor.PingPongProcessor
import PingPongProcessor.{ReceivedBytes, BadHash, Server}
import us.woop.pinger.PongParser
import PongParser.GetServerInfoReply
import scala.concurrent.duration._
import us.woop.pinger.data.actor.IndividualServerProcessor

class IndividualServerRateControlActor(server: Server) extends Act with ActorLogging {

  import IndividualServerProcessor._

  var currentState: ServerState = _
  var badHashes: Int = _
  var totalMessages: Int = _
  var lastReceived: Long = _

  case object SendMetrics

  whenStarting {
    badHashes = 0
    totalMessages = 0
    currentState = Initialising
    context.parent ! Ping
    refreshIn(3.seconds)

    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(1.minute, 1.minute, self, SendMetrics)

    log.info(s"Initialised monitor for $server")
  }

  def refreshIn(duration: FiniteDuration) {
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.scheduleOnce(duration, self, Refresh)
  }

  become {

    case BadHash(`server`, received, _, _, _) =>
      badHashes = badHashes + 1
      totalMessages = totalMessages + 1
      lastReceived = received

    case ReceivedBytes(`server`, received, GetServerInfoReply(serverinforeply)) =>
      val olderState = currentState
      currentState = if (serverinforeply.clients == 0) Online(Empty) else Online(Active)
      if ( currentState != olderState ) context.parent ! currentState
      lastReceived = received

    case ReceivedBytes(`server`, received, _) =>
      totalMessages = totalMessages + 1
      lastReceived = received

    case Refresh =>
      context.parent ! Ping

      refreshIn {
        currentState match {
          case Offline => 30.seconds
          case Initialising => 3.seconds
          case Online(Active) => 3.seconds
          case Online(Empty) => 21.seconds
        }
      }

      if ( currentState == Initialising ) {
        currentState = Offline
        context.parent ! currentState
      }

    case SendMetrics =>
//      context.actorSelection("/user/metrics") !
        val msg = Map(
        'server -> server,
        'currentState -> currentState,
        'badHashes -> badHashes,
        'totalMessages -> totalMessages,
        'lastReceived -> lastReceived
      )

      badHashes = 0
      totalMessages = 0

      log.info("Monitoring status: {}", msg)

    case GetState =>
      sender ! currentState

  }
}
