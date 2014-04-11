package us.woop.pinger.client

import akka.actor.{ActorLogging, ActorRef}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import java.net.InetSocketAddress
import concurrent.duration._
import akka.actor.ActorDSL._


class PingPongProcessor extends Act with ActorLogging {

  import us.woop.pinger.data.PingPongProcessor
  import PingPongProcessor.OutboundMessages
  var lastTime: Long = _
  def now = System.currentTimeMillis()
  whenStarting {
    lastTime = now
    import context.system
    io.IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  val outboundMessages = for { message <- OutboundMessages.all } yield ByteString(message.map{_.toByte}.toArray)
  
  import PingPongProcessor._

  become {
    case Udp.Bound(boundTo) =>
      log.debug("Pinger client bound to socket {}", boundTo)
      val socket = sender()
      context.parent ! Ready(boundTo)
      become(ready(socket, boundTo))
  }

  // Hashes: must ensure that we don't get spoofed data coming in.
  // Also will make sure that we don't receive data out-of-sequence or very very late.
  var hashes = scala.collection.mutable.HashMap[Server, ByteString]()

  // We will convert between InetSocketAddress and (String, Int)
  // This will make pattern matching simpler when interacting with remote actors
  // I have no idea if InetSocketAddress can be passed around nicely between different JVMs.
  // I suspect not.
  val inet2server = scala.collection.mutable.HashMap[InetSocketAddress, Server]()
  val server2inet = scala.collection.mutable.HashMap[Server, InetSocketAddress]()
  val hasher = createHasher

  def ready(send: ActorRef, boundTo: InetSocketAddress): Receive = {

    case Ping(server @ Server(IP(ip), port)) =>
      val inetAddress = server2inet.getOrElseUpdate(server, new InetSocketAddress(ip, port + 1))
      inet2server.getOrElseUpdate(inetAddress, server)
      val hash = hasher.makeHash(server)
      hashes += server -> hash
      for {
        (message, idx) <- outboundMessages.zipWithIndex
        byteString = message ++ hash
      } {
        import context.dispatcher
        context.system.scheduler.scheduleOnce((idx * 5).millis, send, Udp.Send(byteString, inetAddress))
      }

    case receivedMessage @ Udp.Received(receivedBytes, fromWho) if (receivedBytes.length > 13) && (inet2server contains fromWho) =>
      val currentTime = now
      lastTime = if ( lastTime > currentTime ) lastTime + 1 else currentTime
      val hostPair = inet2server(fromWho)
      val expectedHash = hashes(hostPair)
      val (head, tail) = receivedBytes.splitAt(3)
      val (theirHash, message) = tail.splitAt(10)
      val recombined = head ++ message
      theirHash match {
        case `expectedHash` =>
          context.parent ! ReceivedBytes(hostPair, lastTime, recombined)
        case wrongHash =>
          context.parent ! BadHash(hostPair, lastTime, receivedBytes, expectedHash, wrongHash)
      }

    case Udp.Received(otherBytes, fromWho) =>
      log.warning("Message from UDP host {} does not match an acceptable format: {}", fromWho, otherBytes)

  }

}