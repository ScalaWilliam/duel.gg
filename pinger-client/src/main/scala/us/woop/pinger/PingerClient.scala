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

  case class BadHash(bytes: List[Byte])

  case class CannotParse(bytes: List[Byte])

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

class PingerClient(listener: ActorRef) extends Actor with ActorLogging {

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
    case Ping(who@(host, port)) =>
      val inetAddress = pair2inet.getOrElseUpdate(who, new InetSocketAddress(host, port + 1))
      inet2pair.getOrElseUpdate(inetAddress, who)
      targets += who
      log.debug("Received ping request for server {}", who)
      val hash = hasher.makeHash(who)
      hashes += who -> hash
      for {
        (_, message) <- outboundMessages
        hashedMessage = message ::: hash
        hashedArray = hashedMessage.toArray
        byteString = ByteString(hashedArray)
      } {
        log.debug("Sending to {} ({}) data {}", who, inetAddress, byteString)
        send ! Udp.Send(byteString, inetAddress)
      }

    case Udp.Received(receivedBytes, fromWho) if (receivedBytes.length > 13) && (inet2pair contains fromWho) =>
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
            val result = (hostPair, fn(recombined))
            log.debug("Received result: {}", result)
            listener ! result
          } catch {
            case NonFatal(e) =>
              listener !(hostPair, CannotParse(receivedBytes.toList))
              log.error(e,
                "Caught exception when parsing message from server {}. Detail: {}", hostPair,
                Map('head -> head, 'messageToParse -> recombined, 'goodHash -> expectedHash, 'originalMessage -> receivedBytes)
              )
          }

        case wrongHash =>
          listener !(hostPair, BadHash(receivedBytes.toList))
          log.warning(
            "Received wrong hash from server {}. Detail: {}", hostPair,
            Map('head -> head, 'messageToParse -> recombined, 'expectedHash -> expectedHash, 'wrongHash -> wrongHash, 'originalMessage -> receivedBytes)
          )
      }
    case Udp.Received(otherBytes, fromWho) =>
      log.warning("Message from UDP host {} does not match an acceptable format: {}", fromWho, otherBytes)
  }

}