package gg.duel.pinger.service

import java.net.InetSocketAddress

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.io
import akka.io.Udp
import gg.duel.pinger.service.PingPongProcessor._

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
