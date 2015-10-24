package gg.duel.pinger.data.journal

import java.nio.{ByteBuffer, ByteOrder}

import akka.util.{ByteString, ByteStringBuilder}
import gg.duel.pinger.service.{Server, SocketedServer}
import org.joda.time.format.ISODateTimeFormat

case class SauerBytes(socketedServer: SocketedServer, time: Long, message: ByteString) {
  def stringTime = ISODateTimeFormat.dateTimeNoMillis().print(time)

  override def toString = s"SauerBytes($socketedServer, $stringTime, $message)"

  def serialize: ByteString = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = socketedServer.ip.split('.').map(_.toInt.toByte)
    assert(ipBytes.length == 4, s"ipBytes must be of size 4. Input = $socketedServer")
    new ByteStringBuilder()
      .putLong(time)
      .putBytes(ipBytes)
      .putInt(socketedServer.port)
      .append(message)
      .result()
  }

}
object SauerBytes {

  def fromBytes(bytes: ByteString): Option[SauerBytes] = {
    val byteBuffer =
      ByteBuffer
        .wrap(bytes.toArray)
        .order(ByteOrder.BIG_ENDIAN)

    val time = byteBuffer.getLong

    val ip =
      Iterator
        .fill(4)(byteBuffer.get)
        .map(_.toInt & 0xFF)
        .mkString(".")

    val port = byteBuffer.getInt

    val receivedBytes =
      Iterator
        .continually(byteBuffer.hasRemaining)
        .takeWhile(identity)
        .map(_ => byteBuffer.get())
        .toArray

    Server(address = s"$ip:$port").socketedServer.map { server =>
      SauerBytes(
        socketedServer = server,
        time = time,
        message = ByteString(receivedBytes)
      )
    }
  }

}


