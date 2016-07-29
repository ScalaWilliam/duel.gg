package gg.duel.pinger.data

import java.net.{InetAddress, InetSocketAddress}

import org.apache.commons.validator.routines.InetAddressValidator

case class IP(intValue: Int) {
  def bytes: Array[Byte] = {
    var part1 = intValue & 255
    var part2 = (intValue >> 8) & 255
    var part3 = (intValue >> 16) & 255
    var part4 = (intValue >> 24) & 255
    Array(part4.toByte, part3.toByte, part2.toByte, part1.toByte)
  }

  def stringIp = {
    var part1 = intValue & 255
    var part2 = (intValue >> 8) & 255
    var part3 = (intValue >> 16) & 255
    var part4 = (intValue >> 24) & 255

    s"${part4}.${part3}.${part2}.${part1}"
  }
}

object IP {
  def apply(string: String): IP = {
    string.split("\\.").map(_.toInt.toByte) match {
      case Array(a, b, c, d) => IP {
        (a << 24) | (b << 16) | (c << 8) | d
      }
    }
  }

  def valid(ip: String) =
    ip match {
      case valid if InetAddressValidator.getInstance().isValidInet4Address(valid) =>
        IP(ip)
    }
}

case class Server(ip: IP, port: Int) {

  override def hashCode(): Int = {
    ip.intValue + (port << 2)
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case s: Server => s.ip.intValue == ip.intValue && s.port == port
      case _ => false
    }
  }

  def getAddress = s"${ip.stringIp}:$port"

  def getInfoInetSocketAddress = new InetSocketAddress(ip.stringIp, port + 1)
}

object Server {
  val prtRegex = """Server\(IP\(([^,]+)\),([\d]+)\)""".r

  def fromPrinter(string: String): Option[Server] = {
    PartialFunction.condOpt(string) {
      case prtRegex(ip, port) => Server(ip, port.toInt)
    }
  }

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
