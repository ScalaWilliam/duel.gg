package gg.duel.pinger

import java.net.InetSocketAddress

import scala.util.Try

object Hosting {

  object int {
    def unapply(str: String): Option[Int] =
      Try(str.toInt).toOption
  }
  case class Server(address: String) {
    def inetSocketAddress: Option[InetSocketAddress] = {
      val matchStr = s"""(\d+\.\d+\.\d+\.\d+):(\d+)""".r
      PartialFunction.condOpt(address) {
        case matchStr(ip, int(port)) =>
          new InetSocketAddress(ip, port)
      }
    }
  }

  case class Servers(servers: Set[Server])

}