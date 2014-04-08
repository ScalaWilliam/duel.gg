package us.woop.pinger.client.data

import java.net.{InetSocketAddress, InetAddress}
import us.woop.pinger.PingerServiceData
import akka.util.ByteString
import scala.util.Random
import java.security.MessageDigest

object PingPongProcessor {

  case class IP(ip: String)
  case class Server(ip: IP, port: Int)
  object Server {
    def apply(host: String, port: Int): Server =
      Server(IP(InetAddress.getByName(host).getHostAddress), port)
    def apply(host: String): Server =
      apply(host, PingerServiceData.defaultSauerbratenPort)
    def apply(ip: IP): Server =
      Server(ip, PingerServiceData.defaultSauerbratenPort)
  }
  case class BadHash(server: Server, time: Long, fullMessage: ByteString, expectedHash: ByteString, haveHash: ByteString)
  case class ReceivedMessage(server: Server, time: Long, message: ByteString)
  case class Ping(server: Server)
  case class Ready(on: InetSocketAddress)

  def createhasher = new {
    val random = new Random
    val hasher = MessageDigest.getInstance("SHA")

    def makeHash(address: PingPongProcessor.Server): ByteString = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      val hashedBytes = hasher.digest(inputBytes)
      ByteString(hashedBytes.take(10))
    }
  }

  object OutboundMessages {
    val askForServerInfo = List(1, 1, 1)
    val askForServerUptime = List(0, 0, -1)
    val askForPlayerStats = List(0, 1, -1)
    val askForTeamStats = List(0, 2, -1)
    val all = List(askForServerInfo, askForPlayerStats, askForTeamStats, askForServerUptime)
  }

}
