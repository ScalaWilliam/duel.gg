package us.woop.pinger.data

import java.nio.{ByteBuffer, ByteOrder}

import akka.util.{ByteString, ByteStringBuilder}

case class SauerBytes(server: Server, time: Long, message: Vector[Byte]) {
  def toBytes = SauerBytes2.toBytes(this)
}
object SauerBytes {
  def toBytes(sauerBytes: SauerBytes): Array[Byte] = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = sauerBytes.server.ip.ip.split('.').map(_.toInt.toByte)
    assert(ipBytes.size == 4, s"ipBytes must be of size 4. Input = ${sauerBytes.server}")
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
      message = receivedBytes.toVector
    )
  }
}
object SauerBytes2 {
  def toBytes(sauerBytes: SauerBytes): Array[Byte] = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val ipBytes = sauerBytes.server.ip.ip.split('.').map(_.toInt.toByte)
    assert(ipBytes.size == 4, s"ipBytes must be of size 4. Input = ${sauerBytes.server}")
    new ByteStringBuilder().putLong(sauerBytes.time).putBytes(ipBytes).putInt(sauerBytes.server.port).append(ByteString(sauerBytes.message.toArray)).result().toArray
  }
  def fromBytes(bytes: Array[Byte]): SauerBytes = {
    val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    val time = byteBuffer.getLong
    val ip = Iterator.fill(4)(byteBuffer.get).map(_.toInt & 0xFF).mkString(".")
    val port = byteBuffer.getInt
    val receivedBytes = Iterator.continually(byteBuffer.hasRemaining).takeWhile(identity).map(_ => byteBuffer.get()).toArray
    SauerBytes(
      server = Server(ip, port),
      time = time,
      message = receivedBytes.toVector
    )
  }
}