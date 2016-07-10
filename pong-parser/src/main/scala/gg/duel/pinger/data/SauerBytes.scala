package gg.duel.pinger.data

import java.nio.{ByteBuffer, ByteOrder}

import akka.util.{ByteString, ByteStringBuilder}
import org.joda.time.format.ISODateTimeFormat

case class SauerBytes(server: Server, time: Long, message: ByteString) {
  def stringTime = ISODateTimeFormat.dateTimeNoMillis().print(time)
  override def toString = s"SauerBytes($server, $stringTime, $message)"
}

object SauerBytesOld {
  def toBytes(sauerBytes: SauerBytes): Array[Byte] = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = sauerBytes.server.ip.ip.split('.').map(_.toInt.toByte)
    assert(ipBytes.length == 4, s"ipBytes must be of size 4. Input = ${sauerBytes.server}")
    new ByteStringBuilder().putBytes(ipBytes).putInt(sauerBytes.server.port).putLong(sauerBytes.time).append(ByteString(sauerBytes.message.toArray)).result().toArray
  }
  def fromBytes(bytes: Array[Byte]): SauerBytes = {
    val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    val ip = Iterator.fill(4)(byteBuffer.get).map(_.toInt & 0xFF).mkString(".")
    val port = byteBuffer.getInt
    val time = byteBuffer.getLong
    val receivedBytes = Iterator.continually(byteBuffer.hasRemaining).takeWhile(identity).map(_ => byteBuffer.get()).toArray
    SauerBytes(
      server = Server(ip, port),
      time = time,
      message = ByteString(receivedBytes)
    )
  }
}
object SauerBytesBinary {


  def toBytes(sauerBytes: SauerBytes): Array[Byte] = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = sauerBytes.server.ip.ip.split('.').map(_.toInt.toByte)
    assert(ipBytes.length == 4, s"ipBytes must be of size 4. Input = ${sauerBytes.server}")
    new ByteStringBuilder()
      .putLong(sauerBytes.time)
      .putBytes(ipBytes)
      .putInt(sauerBytes.server.port)
      .append(ByteString(sauerBytes.message.toArray))
      .result().toArray
  }




  def fromBytes(bytes: Array[Byte]): SauerBytes = {
    val byteBuffer =
      ByteBuffer
        .wrap(bytes)
        .order(ByteOrder.BIG_ENDIAN)
    fromBuffer(byteBuffer)
  }
  def fromBuffer(byteBuffer: ByteBuffer): SauerBytes = {

    val time = byteBuffer.getLong

    val ip = IP(
      (byteBuffer.get.toShort & 0xFF).toShort + "." +
      (byteBuffer.get.toShort & 0xFF).toShort + "." +
      (byteBuffer.get.toShort & 0xFF).toShort + "." +
      (byteBuffer.get.toShort & 0xFF).toShort
    )

    val port = byteBuffer.getInt

    val receivedBytes = byteBuffer.array().drop(byteBuffer.position)

    SauerBytes(
      server = Server(ip, port),
      time = time,
      message = ByteString(receivedBytes)
    )
  }

}


