package us.woop.pinger

import scala.concurrent.duration.FiniteDuration
import java.net.InetAddress

object PingerServiceData {
  val defaultSauerbratenPort = 28785
  case class Subscribe(server: Server, requireRate: Option[FiniteDuration] = None)
  object Subscribe {
    def apply(server: Server, duration: FiniteDuration): Subscribe =
      Subscribe(server, Option(duration))
  }
  case class Unsubscribe(server: Server)
  case class Server(ip: String, port: Int)
  case class ChangeRate(server: Server, rate: FiniteDuration)
  case class SauerbratenPong(unixTime: Long, host: (String, Int), payload: Any)
  case object Firehose
  case object Unfirehose
  object Server {
    def unapply(from: (String, Int)): Option[Server] =
      Option(PingerServiceData.Server(from._1, from._2))
    def apply(address: InetAddress, port: Int): Server =
      Server(address.getHostAddress, port)
    def apply(address: InetAddress): Server =
      apply(address, defaultSauerbratenPort)
    def apply(ip: String): Server =
      apply(ip, defaultSauerbratenPort)
  }
}
