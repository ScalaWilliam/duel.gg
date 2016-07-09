package gg.duel.pinger.service

import java.net.InetSocketAddress

import akka.io.Udp
import akka.util.ByteString
import gg.duel.pinger.data.Server
import gg.duel.pinger.service.PingPongProcessor._

import scala.concurrent.duration._

/**
  * Created by me on 09/07/2016.
  */
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
