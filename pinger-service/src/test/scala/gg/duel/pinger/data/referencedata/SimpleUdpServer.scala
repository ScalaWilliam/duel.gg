package gg.duel.pinger.data.referencedata

import java.net.InetSocketAddress

import akka.actor.ActorDSL._
import akka.actor.{Props, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import akka.io.{IO, Udp}
import akka.util.ByteString

/** 01/02/14 */
object SimpleUdpServer {

  case class Ready(address: InetSocketAddress)

  class SauerbratenPongServer(address: InetSocketAddress) extends Act with ActorLogging {

    whenStarting {
      import context.system
      IO(Udp) ! Udp.Bind(self, address)
    }

    type MapsBytes = PartialFunction[Vector[Int], Vector[Vector[Int]]]

    val recombine = (_: Vector[Int]) ++ (_: Vector[Int]) ++ (_: Vector[Int])

    def mapped(socket: ActorRef, mappings: MapsBytes) = LoggingReceive {
      case message @ Udp.Received(data, clientAddress) if mappings.isDefinedAt(data.map(_.toInt).toVector) =>
        log.debug("Server at {} received input: {}", address, message)
        val dataBytes = data.map(_.toInt).toVector
        val head = dataBytes.take(3)
        val hash = dataBytes.drop(3)
        mappings.apply(dataBytes).map(recombine(head, hash, _)).map{
          x =>
            ByteString(x.map(_.toByte) :_*)
        }.map(Udp.Send(_, clientAddress)).foreach{ response =>
          log.debug("Server at {} responded with: {}", address, response)
          socket ! response
        }
    }

    def restOfUdp(socket: ActorRef) = LoggingReceive {
      case Udp.Unbind =>
        socket ! Udp.Unbind
      case Udp.Unbound =>
        context.stop(self)
    }

    becomeStart(restOfUdp)

    def becomeStart(fn: ActorRef => Receive) {
      become {
        case Udp.Bound(boundToAddress) =>
          context.parent ! Ready(boundToAddress)
          become(fn(sender()))
      }
    }

  }

  class GoodHashSauerbratenPongServer(address: InetSocketAddress) extends SauerbratenPongServer(address) {
    becomeStart { sender =>
      mapped(sender, StubServer.mappings) orElse restOfUdp(sender)
    }
  }
  object GoodHashSauerbratenPongServer {
    def props(address: InetSocketAddress) = Props(classOf[GoodHashSauerbratenPongServer], address)
  }
  class BadHashSauerbratenPongServer(address: InetSocketAddress) extends SauerbratenPongServer(address) {

    override val recombine = (_: Vector[Int]) ++ Vector(1) ++ (_: Vector[Int]) ++ (_: Vector[Int])

    becomeStart { sender =>
      mapped(sender, StubServer.mappings) orElse restOfUdp(sender)
    }
  }
  object BadHashSauerbratenPongServer {
    def props(address: InetSocketAddress) = Props(classOf[BadHashSauerbratenPongServer], address)
  }

}