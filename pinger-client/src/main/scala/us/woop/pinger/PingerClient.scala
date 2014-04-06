package us.woop.pinger

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import java.net.InetSocketAddress
import java.security.MessageDigest
import scala.util.control.NonFatal
import scala.util.Random

/** 01/02/14 */

object PingerClient {
  type InetPair = (String, Int)

  case class BadHash(host: InetPair, message: Udp.Received)

  case class CannotParse(host: InetPair, message: Udp.Received)

  case class ParsedMessage(host: InetPair, recombined: List[Byte], transformed: Any)

  case class Ping(host: InetPair)

  case class Ready(on: InetSocketAddress)

  def createhasher = new {
    val random = new Random
    val hasher = MessageDigest.getInstance("SHA")

    def makeHash(address: PingerClient.InetPair): List[Byte] = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      val hashedBytes = hasher.digest(inputBytes)
      val postfix = hashedBytes.toList.take(10)
      postfix
    }
  }
}

/**
 *
 * Outputs the following messages:
 * — Ready
 * — BadHash
 * — CannotParse
 * — ParsedMessage
 *
 */

class PingerClient(val listenerRequested: Option[ActorRef] = None) extends Actor with ActorLogging {

  def this() = this(None)

  def this(ref: ActorRef) = this(Option(ref))

  var listener: ActorRef = _

  override def preStart() {
    super.preStart()
    listener = listenerRequested match {
      case Some(actor) => actor
      case None => context.parent
    }
  }

  import PingerClient._

  val myAddress = new InetSocketAddress("0.0.0.0", 0)

  import context.system

  io.IO(Udp) ! Udp.Bind(self, myAddress)

  def receive = {
    case Udp.Bound(boundTo) =>
      log.debug("Pinger client bound to socket {}", boundTo)
      val socket = sender()
      listener ! Ready(boundTo)
      context.become(ready(socket))
  }

  // Hashes: must ensure that we don't get spoofed data coming in.
  // Also will make sure that we don't receive data out-of-sequence or very very late.
  var hashes = scala.collection.mutable.HashMap[InetPair, List[Byte]]()

  // We will convert between InetSocketAddress and (String, Int)
  // This will make pattern matching simpler when interacting with remote actors
  // I have no idea if InetSocketAddress can be passed around nicely between different JVMs.
  // I suspect not.
  val inet2pair = scala.collection.mutable.HashMap[InetSocketAddress, InetPair]()
  val pair2inet = scala.collection.mutable.HashMap[InetPair, InetSocketAddress]()
  val hasher = createhasher

  val outboundMessages = Map(
    'askForServerInfo -> List(1, 1, 1).map(_.toByte),
    'askForServerUptime -> List(0, 0, -1).map(_.toByte),
    'askForPlayerStats -> List(0, 1, -1).map(_.toByte),
    'askForTeamStats -> List(0, 2, -1).map(_.toByte)
  )

  val targets = scala.collection.mutable.HashSet[InetPair]()

  def ready(send: ActorRef): Receive = {
    case Ping(who@(host:String, port: Int)) =>
      val inetAddress = pair2inet.getOrElseUpdate(who, new InetSocketAddress(host, port + 1))
      inet2pair.getOrElseUpdate(inetAddress, who)
      targets += who
      log.debug("Received ping request for server {}", who)
      val hash = hasher.makeHash(who)
      hashes += who -> hash
      for {
        ((_, message), idx) <- outboundMessages.zipWithIndex
        hashedMessage = message ::: hash
        hashedArray = hashedMessage.toArray
        byteString = ByteString(hashedArray)
      } {
        log.debug("Sending to {} ({}) data {}", who, inetAddress, byteString)
        import scala.concurrent.duration._
        import context.dispatcher
        context.system.scheduler.scheduleOnce((idx * 5).millis, send, Udp.Send(byteString, inetAddress))
      }

    case receivedMessage @ Udp.Received(receivedBytes, fromWho) if (receivedBytes.length > 13) && (inet2pair contains fromWho) =>
      val hostPair = inet2pair(fromWho)
      val expectedHash = hashes(hostPair)

      val head = receivedBytes.take(3).toList
      val theirHash = receivedBytes.drop(3).take(10).toList
      val message = receivedBytes.drop(3).drop(10).toList
      val recombined = head ::: message

      theirHash match {
        case `expectedHash` =>
          val fn: PartialFunction[List[Byte], Any] = SauerbratenProtocol.matchers
          try {
            import Extractor.extract
            val items = extract apply recombined
            for { item <- items } {
              val parsedMessage = ParsedMessage(hostPair, recombined, item)
              log.debug("Received result: {}", parsedMessage)
              listener ! parsedMessage
            }
          } catch {
            case NonFatal(e) =>
              listener ! CannotParse(hostPair, receivedMessage)
              log.error(e,
                "Caught exception when parsing message from server {}. Detail: {}", hostPair,
                Map('head -> head, 'messageToParse -> recombined, 'goodHash -> expectedHash, 'originalMessage -> receivedBytes)
              )
          }

        case wrongHash =>
          listener ! BadHash(hostPair, receivedMessage)
          log.warning(
            "Received wrong hash from server {}. Detail: {}", hostPair,
            Map('head -> head, 'expectedHash -> expectedHash, 'wrongHash -> wrongHash,'messageToParse -> recombined,  'originalMessage -> receivedBytes)
          )
      }
    case Udp.Received(otherBytes, fromWho) =>
      log.warning("Message from UDP host {} does not match an acceptable format: {}", fromWho, otherBytes)
  }

}