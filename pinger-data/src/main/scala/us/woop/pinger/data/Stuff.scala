package us.woop.pinger.data

import java.net.InetAddress

object Stuff {

  case class IP(ip: String)
  case class Server(ip: IP, port: Int)

  object Server {
    def apply(host: String, port: Int): Server =
      Server(IP(InetAddress.getByName(host).getHostAddress), port)
    def apply(host: String): Server =
      apply(host, defaultSauerbratenPort)
    def apply(ip: IP): Server =
      Server(ip, defaultSauerbratenPort)

  }

  val defaultSauerbratenPort = 28765

}
