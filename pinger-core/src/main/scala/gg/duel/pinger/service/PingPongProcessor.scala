package gg.duel.pinger.service

import java.net.InetSocketAddress
import java.security.MessageDigest

import akka.util.ByteString
import gg.duel.pinger.data.{SauerBytes, SauerBytesBinary, Server}

import scala.util.Random

/**
  * Created by me on 09/07/2016.
  */
object PingPongProcessor {

  sealed trait ReceiveResult
  case class BadHash(server: Server, time: Long, fullMessage: ByteString, expectedHash: ByteString, haveHash: ByteString) extends ReceiveResult
  case class ReceivedBytes(server: Server, time: Long, message: ByteString) extends ReceiveResult {
    def toSauerBytes = SauerBytes(server, time, message)
    def toBytes = SauerBytesBinary.toBytes(toSauerBytes)
  }
  object ReceivedBytes {
    def fromSauerBytes(sauerBytes: SauerBytes) =
      ReceivedBytes(sauerBytes.server, sauerBytes.time, sauerBytes.message)
  }
  case class Ping(server: Server)
  case class Ready(on: InetSocketAddress)

  def createHasher = new {
    val random = new Random
    val hasher = MessageDigest.getInstance("SHA")

    def makeHash(address: Server): ByteString = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      val hashedBytes = hasher.digest(inputBytes)
      ByteString(hashedBytes.take(10))
    }
  }

  object OutboundMessages {
    val askForServerInfo = Vector(1, 1, 1)
    val askForServerUptime = Vector(0, 0, -1)
    val askForPlayerStats = Vector(0, 1, -1)
    val askForTeamStats = Vector(0, 2, -1)
    //    val all = List(askForServerInfo, askForPlayerStats, askForTeamStats, askForServerUptime)
    val all = Vector(askForServerInfo, askForPlayerStats, askForTeamStats)
  }


}
