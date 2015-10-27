package gg.duel.pinger.data

import java.net.{InetSocketAddress, InetAddress}

import org.apache.commons.validator.routines.InetAddressValidator

case class IP(ip: String)

object IP {
  def valid(ip: String) =
    ip match {
      case valid if InetAddressValidator.getInstance().isValidInet4Address(valid) =>
        IP(ip)
    }
}

case class Server(ip: IP, port: Int) {
  def getAddress = s"${ip.ip}:$port"
  def getInfoInetSocketAddress = new InetSocketAddress(ip.ip, port + 1)
}

object Server {
  def stub = Server(
    ip = "127.0.0.1",
    port = 2234
  )
  val regex = """([^:]+)(:| )([\d]+)""".r
  def fromInfoInetSocketAddress(address: InetSocketAddress) =
    Server(address.getHostName, address.getPort - 1)
  def fromAddress(address: String): Server = {
    address match {
      case regex(host, _, port) =>
        apply(InetAddress.getByName(host).getHostAddress, port.toInt)
      case host =>
        apply(InetAddress.getByName(host).getHostAddress, defaultSauerbratenPort)
    }
  }
  def apply(ip: String, port: Int): Server =
    Server(IP.valid(ip), port)
  def apply(ipPort: String): Server = {
    ipPort match {
      case regex(ip, _, port) => Server(ip, port.toInt)
    }
  }
  val defaultSauerbratenPort = 28785
}
