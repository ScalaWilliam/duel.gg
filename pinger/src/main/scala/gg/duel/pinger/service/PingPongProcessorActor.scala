package gg.duel.pinger.service

import java.net.InetSocketAddress
import java.security.MessageDigest
import akka.actor.ActorDSL._
import akka.actor.{Props, ActorLogging, ActorRef}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.journal.SauerBytes
import gg.duel.pinger.service.PingPongProcessor._

import scala.util.Random

object PingPongProcessor {

  sealed trait ReceiveResult
  case class BadHash(server: Server, time: Long, fullMessage: ByteString, expectedHash: ByteString, haveHash: ByteString) extends ReceiveResult
  case class ReceivedBytes(server: Server, time: Long, message: ByteString) extends ReceiveResult {
    def toSauerBytes = SauerBytes(server, time, message)
  }
  object ReceivedBytes {
    def fromSauerBytes(sauerBytes: SauerBytes) = 
      ReceivedBytes(sauerBytes.server, sauerBytes.time, sauerBytes.message)
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
    val all = Vector(askForServerInfo, askForPlayerStats, askForTeamStats)
  }


}

import scala.concurrent.duration._

object PingPongProcessorState {
  def empty =
    PingPongProcessorState(
      disableHashing = false,
      lastTime = System.currentTimeMillis(),
      hashes = Map.empty,
      inet2server = Map.empty,
      server2inet = Map.empty
    )
}
case class PingPongProcessorState
(disableHashing: Boolean, lastTime: Long, hashes: Map[Server, ByteString], inet2server: Map[InetSocketAddress, Server], server2inet: Map[Server, InetSocketAddress]) {
  val outboundMessages = for { message <- OutboundMessages.all } yield ByteString(message.map{_.toByte}.toArray)

  val hasher = createHasher
  def ping(server: Server) = {
    val inetAddress = server2inet.getOrElse(server, server.getInfoInetSocketAddress)
    val hash = hasher.makeHash(server)
    val messages = for {
      (message, idx) <- outboundMessages.zipWithIndex
      byteString = message ++ hash
    } yield {
      ((idx * 15).millis, byteString, inetAddress)
    }
    val nextState = copy(
      hashes = hashes + (server -> hash),
      inet2server = inet2server + (inetAddress -> server),
      server2inet = server2inet + (server -> inetAddress)
    )
    (messages, nextState)
  }

  def receive(received: Udp.Received) = {
    PartialFunction.condOpt(received) {
      case receivedMessage @ Udp.Received(receivedBytes, fromWho)
        if (receivedBytes.length > 13) && (inet2server contains fromWho) =>
        val nextTime = System.currentTimeMillis()
        val hostPair = inet2server(fromWho)
        val expectedHash = hashes(hostPair)
        val (head, tail) = receivedBytes.splitAt(3)
        val (theirHash, message) = tail.splitAt(10)
        val recombined = head ++ message
        val processResult = if (disableHashing || theirHash == expectedHash) {
          ReceivedBytes(hostPair, nextTime, recombined)
        } else {
          BadHash(hostPair, nextTime, receivedBytes, expectedHash, theirHash)
        }
        val nextState = copy(lastTime = nextTime)
        (processResult, nextState)

    }
  }
}

object PingPongProcessorActor {
  def props(initialState: PingPongProcessorState) = Props(classOf[PingPongProcessorActor], initialState)
}
class PingPongProcessorActor(initialState: PingPongProcessorState) extends Act with ActorLogging {

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
      become(ready(socket, boundTo, initialState))
  }

  val hasher = createHasher

  def ready(send: ActorRef, boundTo: InetSocketAddress, pingPongProcessorState: PingPongProcessorState): Receive = {
    case Ping(server) =>
      pingPongProcessorState.ping(server) match {
        case (messages, nextState) =>
          messages.foreach { case x@ (delay, data, target) =>
            import context.dispatcher
            context.system.scheduler.scheduleOnce(delay, send, Udp.Send(data, target))
          }
          become(ready(send, boundTo, nextState))
      }

    case receivedMessage @ Udp.Received(bytes, udpSender) =>
      pingPongProcessorState.receive(receivedMessage) match {
        case Some((stuff, nextState)) =>
          context.parent ! stuff
          become(ready(send, boundTo, nextState))
        case None =>
          log.warning("Message from UDP host {} does not match an acceptable format: {}", udpSender, bytes)
      }

  }

}