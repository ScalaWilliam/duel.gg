package gg.duel.pinger.data

import java.nio.{ByteBuffer, ByteOrder}

import akka.util.{ByteString, ByteStringBuilder}
import org.joda.time.format.ISODateTimeFormat

case class SauerBytes(server: Server, time: Long, message: ByteString) {
  def stringTime = ISODateTimeFormat.dateTimeNoMillis().print(time)

  override def toString = s"SauerBytes($server, $stringTime, $message)"
}

object SauerBytes {
  def fromArray(tmpArr: Array[Byte], dataLength: Int): SauerBytes = {
    val time = longFrom(tmpArr, 0)
    val ip = IP(intFrom(tmpArr, 8))
    val port = intFrom(tmpArr, 12)
    SauerBytes(
      server = Server(ip, port),
      time = time,
      message = ByteString.fromArray(tmpArr, 16, dataLength - 16)
    )
  }

  def intFrom(array: Array[Byte], pos: Int): Int = {
    (array(pos) << 24) | ((array(pos + 1) & 0xff) << 16) |
      ((array(pos + 2) & 0xff) << 8) | (array(pos + 3) & 0xff)
  }

  def longFrom(readBuffer: Array[Byte], pos: Int): Long = {
    (readBuffer(0).toLong << 56) + ((readBuffer(1) & 255).toLong << 48) + ((readBuffer(2) & 255).toLong << 40) + ((readBuffer(3) & 255).toLong << 32) + ((readBuffer(4) & 255).toLong << 24) + ((readBuffer(5) & 255) << 16) + ((readBuffer(6) & 255) << 8) + ((readBuffer(7) & 255) << 0)
  }
}

object SauerBytesBinary {

  def toBytes(sauerBytes: SauerBytes): Array[Byte] = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = sauerBytes.server.ip.bytes
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


