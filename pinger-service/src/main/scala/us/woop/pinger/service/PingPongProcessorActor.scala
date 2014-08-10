package us.woop.pinger.service

import java.net.InetSocketAddress
import java.security.MessageDigest

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import us.woop.pinger.data.Server
import us.woop.pinger.service.PingPongProcessor._

import scala.util.Random

object PingPongProcessor {

  case class BadHash(server: Server, time: Long, fullMessage: ByteString, expectedHash: ByteString, haveHash: ByteString)
  case class ReceivedBytes(server: Server, time: Long, message: ByteString) {
    def toSerializable = SerializableBytes(server, time, message.toArray)
  }
  case class SerializableBytes(server: Server, time: Long, message: Array[Byte]) {
    def toReceived = ReceivedBytes(server, time, ByteString(message))
  }
  case class Ping(server: Server)
  case class Ready(on: InetSocketAddress)

  def createHasher = new {
    val random = new Random
    val hasher = MessageDigest.getInstance("SHA")

    def makeHash(address: Server): ByteString = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      val hashedBytes = hasher.digest(inputBytes)
      ByteString(hashedBytes.take(10))
    }
  }

  object OutboundMessages {
    val askForServerInfo = Vector(1, 1, 1)
    val askForServerUptime = Vector(0, 0, -1)
    val askForPlayerStats = Vector(0, 1, -1)
    val askForTeamStats = Vector(0, 2, -1)
    //    val all = List(askForServerInfo, askForPlayerStats, askForTeamStats, askForServerUptime)
    val all = Vector(askForPlayerStats, askForTeamStats, askForServerInfo)
  }

}

import scala.concurrent.duration._

class PingPongProcessorActor extends Act with ActorLogging {

  val outboundMessages = for { message <- OutboundMessages.all } yield ByteString(message.map{_.toByte}.toArray)

  def now = System.currentTimeMillis

  whenStarting {
    log.info("Starting raw pinger...")
    import context.system
    io.IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  become {
    case Udp.Bound(boundTo) =>
      log.debug("Pinger client bound to socket {}", boundTo)
      val socket = sender()
      context.parent ! Ready(boundTo)
      become(ready(socket, boundTo, now, Map.empty, Map.empty,Map.empty))
  }

  val hasher = createHasher

  def ready(send: ActorRef, boundTo: InetSocketAddress, lastTime: Long, hashes: Map[Server, ByteString], inet2server: Map[InetSocketAddress, Server], server2inet: Map[Server, InetSocketAddress]): Receive = {

    case Ping(server) =>
      val inetAddress = server2inet.getOrElse(server, server.getInfoInetSocketAddress)
      val hash = hasher.makeHash(server)
      for {
        (message, idx) <- outboundMessages.zipWithIndex
        byteString = message ++ hash
      } {
        import context.dispatcher
        context.system.scheduler.scheduleOnce((idx * 15).millis, send, Udp.Send(byteString, inetAddress))
      }
      become(ready(send, boundTo, lastTime, hashes + (server -> hash), inet2server + (inetAddress -> server), server2inet + (server -> inetAddress)))

    case receivedMessage @ Udp.Received(receivedBytes, fromWho) if (receivedBytes.length > 13) && (inet2server contains fromWho) =>
      val nextTime = now
      val hostPair = inet2server(fromWho)
      val expectedHash = hashes(hostPair)
      val (head, tail) = receivedBytes.splitAt(3)
      val (theirHash, message) = tail.splitAt(10)
      val recombined = head ++ message
      if (theirHash == expectedHash) {
        context.parent ! ReceivedBytes(hostPair, nextTime, recombined)
      } else {
        context.parent ! BadHash(hostPair, nextTime, receivedBytes, expectedHash, theirHash)
      }
      become(ready(send, boundTo, nextTime, hashes, inet2server, server2inet))

    case Udp.Received(otherBytes, fromWho) =>
      log.warning("Message from UDP host {} does not match an acceptable format: {}", fromWho, otherBytes)

  }

}