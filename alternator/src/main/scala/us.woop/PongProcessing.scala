package us.woop

import scala.util.control.NonFatal
import akka.actor.{ActorLogging, Actor}
import us.woop.PongProcessing.FailedProcessing
import us.woop.SauerbratenPinger.{InetPair, ReceivedMessage}
import akka.util.ByteString

/** 02/02/14 */
object PongProcessing {
  case class ProcessedMessage[T](hostPair: InetPair, contents: T)
  case class FailedProcessing(message: ReceivedMessage, cause: String)
  def processMessage(message: ReceivedMessage): ProcessedMessage[_]= {

    val fn = SauerbratenProtocol.matchers
    val haveResult = fn(message.message.toList)
    import SauerbratenProtocol._
    haveResult match {
      case GetRelaxedPlayerExtInfo(x) =>
        ProcessedMessage(message.from, x)
      case GetServerInfoReply(x) =>
        ProcessedMessage(message.from, x)
      case GetPlayerCns(x) =>
        ProcessedMessage(message.from, x)
      case GetHopmodUptime(x) =>
        ProcessedMessage(message.from, x)
      case GetTeamScores(x) =>
        ProcessedMessage(message.from, x)
      case GetUptime(x) =>
        ProcessedMessage(message.from, x)
      case GetThomasModExtInfo(x) =>
        ProcessedMessage(message.from, x)
      case CheckOlderClient(x) =>
        throw new Exception(s"We don't like older clients: $x")
        ProcessedMessage(message.from, x)



    }
  }
}
class PongProcessing extends Actor with ActorLogging {

  def receive = {
    case message: ReceivedMessage =>
      try {
        sender ! PongProcessing.processMessage(message)
      } catch {
        case NonFatal(e) =>
          sender ! FailedProcessing(message, e.toString)
          log.error(e, "Parsing message from server failed: {}", message)
      }
  }
}
