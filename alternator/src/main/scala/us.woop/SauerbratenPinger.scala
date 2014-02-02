package us.woop
import akka.actor.{Stash, ActorLogging, Actor, ActorRef}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import java.net.InetSocketAddress
import java.security.MessageDigest
import scala.util.control.NonFatal
import scala.util.Random
/** 01/02/14 */

object SauerbratenPinger {

  case class InetPair(ip: String, port: Int)

  case class ReceivedMessage(from: InetPair, message: ByteString)
  case class ReceivedBadMessage(from: InetPair, bytes: ByteString)

  case class Ping(host: InetPair)

  def createhasher = new {
    val random = new Random
    val hasher = MessageDigest.getInstance("SHA")

    def makeHash(address: InetPair): ByteString = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      ByteString(hasher.digest(inputBytes).take(10))
    }
  }

}

class SauerbratenPinger extends Actor with ActorLogging with Stash {
  import SauerbratenPinger._
  val myAddress = new InetSocketAddress("0.0.0.0", 0)
  import context.system
  io.IO(Udp) ! Udp.Bind(self, myAddress)

  def receive = {
    case Udp.Bound(boundTo) =>
      log.debug("Pinger client bound to socket {}", boundTo)
      val socket = sender
      context.become(ready(socket))
    // We'll ignore any messages before we've initialised.
    // Stashing makes no sense as we want stuff that's up to date in any case.
  }

  // Hashes: must ensure that we don't get spoofed data coming in.
  // Also will make sure that we don't receive data out-of-sequence or very very late.
  var hashes = scala.collection.mutable.HashMap[InetPair, ByteString]()

  // We will convert between InetSocketAddress and (String, Int)
  // This will make pattern matching simpler when interacting with remote actors
  // I have no idea if InetSocketAddress can be passed around nicely between different JVMs.
  // I suspect not.
  val inet2pair = scala.collection.mutable.HashMap[InetSocketAddress, InetPair]()
  val pair2inet = scala.collection.mutable.HashMap[InetPair, InetSocketAddress]()
  val hasher = createhasher

  val outboundMessages = Map(
    'askForServerInfo -> ByteString(1, 1, 1),
    'askForServerUptime -> ByteString(0, 0, -1),
    'askForPlayerStats -> ByteString(0, 1, -1),
    'askForTeamStats -> ByteString(0, 2, -1)
  )

  val targets = scala.collection.mutable.HashSet[InetPair]()

  def ready(send: ActorRef): Receive = {
    case Ping(who@InetPair(host, port)) =>
      val inetAddress = pair2inet.getOrElseUpdate(who, new InetSocketAddress(host, port + 1))
      inet2pair.getOrElseUpdate(inetAddress, who)
      targets += who
      log.debug("Received ping request for server {}", who)
      val hash = hasher.makeHash(who)
      hashes += who -> hash
      for {
        (_, message) <- outboundMessages
        byteString = message ++ hash
      } {
        log.debug("Sending to {} ({}) data {}", who, inetAddress, byteString)
        send ! Udp.Send(byteString, inetAddress)
      }

    case Udp.Received(receivedBytes, fromWho) if (receivedBytes.length > 13) && (inet2pair contains fromWho) =>
      val hostPair = inet2pair(fromWho)
      val expectedHash = hashes(hostPair)

      val head = receivedBytes.take(3)
      val theirHash = receivedBytes.drop(3).take(10)
      val message = receivedBytes.drop(3).drop(10)
      val recombined = head ++ message

      theirHash match {
        case `expectedHash` =>
          context.parent ! ReceivedMessage(hostPair, recombined)
          
        case wrongHash =>
          context.parent ! ReceivedBadMessage(hostPair, receivedBytes)
          log.warning(
            "Received wrong hash from server {}. Detail: {}", hostPair,
            Map('head -> head, 'messageToParse -> recombined, 'expectedHash -> expectedHash, 'wrongHash -> wrongHash, 'originalMessage -> receivedBytes)
          )
      }
    case Udp.Received(otherBytes, fromWho) =>
      log.warning("Message from UDP host {} does not match an acceptable format: {}", fromWho, otherBytes)
  }

}