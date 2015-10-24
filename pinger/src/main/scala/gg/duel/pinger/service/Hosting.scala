package gg.duel.pinger.service

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.io
import akka.io.Udp
import akka.util.ByteString
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZFoundDuel, ZFoundCtf, ZIteratorState}
import gg.duel.pinger.data.Extractor
import gg.duel.pinger.data.ParsedPongs.ParsedMessage

import scala.util.Try


object int {
  def unapply(str: String): Option[Int] =
    Try(str.toInt).toOption
}

case class Server(address: String) {
  def socketedServer: Option[SocketedServer] =
    inetSocketAddress.map { isa =>
      SocketedServer(
        server = this,
        inetSocketAddress = isa
      )
    }

  def inetSocketAddress: Option[InetSocketAddress] = {
    val matchStr = s"""(\d+\.\d+\.\d+\.\d+):(\d+)""".r
    PartialFunction.condOpt(address) {
      case matchStr(ip, int(port)) =>
        Try(new InetSocketAddress(ip, port + 1)).toOption
    }.flatten
  }
}

case class SocketedServer(server: Server, inetSocketAddress: InetSocketAddress) {
  def simpleText = s"${ip}_$port"

  def ip = inetSocketAddress.getHostString

  def port = inetSocketAddress.getPort
}

case class SocketedServers(servers: Set[SocketedServer])
object SocketedServers {
  def empty = SocketedServers(
    servers = Set.empty
  )
}

import akka.actor.ActorDSL._

class PingPong(socketedServer: SocketedServer) extends Act with ActorLogging {

  whenStarting {
    log.info(s"Starting raw pinger for server $socketedServer...")
    import context.system
    io.IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  become {
    case Udp.Bound(boundTo) =>
      log.debug("Pinger client bound to socket {}", boundTo)
      val socket = sender()
      become {
        case Ping =>
          PingPong.OutboundMessages.all.zipWithIndex.foreach {
            case (message, idx) =>
              import context.dispatcher

              import concurrent.duration._
              val delay = (idx * 15).millis
              context.system.scheduler.scheduleOnce(
                delay = delay,
                receiver = socket,
                message = Udp.Send(
                  data = ByteString(message: _*),
                  target = socketedServer.inetSocketAddress
                )
              )
          }
        case receivedMessage@Udp.Received(bytes, udpSender)
          if udpSender == socketedServer.inetSocketAddress =>
          context.parent ! PingPong.ReceivedMBytes(
            time = System.currentTimeMillis(),
            message = bytes
          )
      }
  }
}

case object Ping

object PingPong {

  def props(socketedServer: SocketedServer) = Props(
    creator = new PingPong(
      socketedServer = socketedServer
    )
  )

  case class ReceivedMBytes(time: Long, message: ByteString)

  object OutboundMessages {
    val askForServerInfo = Vector(1, 1, 1)
    val askForServerUptime = Vector(0, 0, -1)
    val askForPlayerStats = Vector(0, 1, -1)
    val askForTeamStats = Vector(0, 2, -1)
    //    val all = List(askForServerInfo, askForPlayerStats, askForTeamStats, askForServerUptime)
    val all = Vector(askForServerInfo, askForPlayerStats, askForTeamStats)
  }

}


case class ServerState(server: SocketedServer, actor: ActorRef, iteratorState: ZIteratorState)

//val (enumerator, channel) = Concurrent.broadcast[String]

object MultiplePinger {

  case object Ping

  def props = Props(new MultiplePinger)

}

class MultiplePinger extends Act {

  whenStarting {
    become(processing(
      existingStates = Map.empty
    ))
  }

  def processing(existingStates: Map[SocketedServer, ServerState]): Receive = {
    case MultiplePinger.Ping =>
    case updateServers: SocketedServers =>
      val newServers = updateServers.servers -- existingStates.keySet
      val newStates = existingStates ++ newServers.map{ server =>
        server -> ServerState(
          server = server,
          actor = context.actorOf(PingPong.props(server)),
          iteratorState = ZIteratorState.initial
        )
      }.toMap
      become(processing(
        existingStates = newStates
      ))
    case PingPong.ReceivedMBytes(time, message) =>
      existingStates.find(_._2.actor == sender()).foreach {
        case (server, state) =>
          Extractor.extract.lift.apply(message).flatMap(_.headOption).map { pm =>
            ParsedMessage(
              time = time,
              message = pm
            )
          }.map { parsedMessage =>
            state.iteratorState.next.apply(parsedMessage)
          }.foreach { newState =>
            become {
              processing(
                existingStates = existingStates + (server -> state.copy(iteratorState = newState))
              )
            }
            PartialFunction.condOpt(newState) match {
              case ZFoundDuel(_, duel) =>
                context.parent ! duel
              case ZFoundCtf(_, ctf) =>
                context.parent ! ctf
            }
          }
      }
  }
}
