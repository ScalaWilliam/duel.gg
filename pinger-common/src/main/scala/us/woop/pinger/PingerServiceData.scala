package us.woop.pinger

import scala.concurrent.duration.FiniteDuration

object PingerServiceData {
  case class Subscribe(server: Server)
  case class Unsubscribe(server: Server)
  case class Server(ip: String, port: Int)
  case class ChangeRate(server: Server, rate: FiniteDuration)
  case class SauerbratenPong(unixTime: Long, host: (String, Int), payload: Any)
  object Server {
    def unapply(from: (String, Int)): Option[Server] =
      Option(PingerServiceData.Server(from._1, from._2))
  }
}
