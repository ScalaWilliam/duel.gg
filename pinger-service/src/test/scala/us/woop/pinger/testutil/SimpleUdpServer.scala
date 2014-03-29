package us.woop.pinger.testutil

import akka.actor.Actor._
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorSystem, ActorRef}
import akka.event.LoggingReceive
import akka.io.{Udp, IO}
import akka.util.ByteString
import java.net.InetSocketAddress

/** 01/02/14 */
object SimpleUdpServer {

  type Respond2 = PartialFunction[(List[Int] => Unit, List[Int]), Unit]

  case class Ready(to: InetSocketAddress)

  def udpServer2(local: InetSocketAddress, status: ActorRef)(respond: Respond2)(orMore: Receive = emptyBehavior)(implicit actorSystem: ActorSystem) =
    actor(actorSystem, s"""server:${local.toString.replaceAllLiterally("/", "")}""")(new Act with ActorLogging {
      IO(Udp) ! Udp.Bind(self, local)

      def responding(socket: ActorRef): Receive = LoggingReceive {
        case a@Udp.Received(data, from) =>

          val respondFunction = (bytes: List[Int]) => {
            socket ! Udp.Send(ByteString(bytes.map(_.toByte): _*), from)
          }
          val process: Respond2 = respond orElse {
            case a@(aa, b) =>
              println(s"Not matched: $a")
              log.info(s"Ignored client request from '$from' with data '$data'")
          }

          process((respondFunction, data.map(_.toInt).toList))

        case Udp.Unbind =>
          socket ! Udp.Unbind
        case Udp.Unbound =>
          context.stop(self)
      }

      become {

        case Udp.Bound(boundToAddress) =>
          val socket = sender()
          log.info(s"Server successfully bound to $boundToAddress")
          become(responding(socket) orElse orMore)
          status ! Ready(boundToAddress)
      }
    })

}