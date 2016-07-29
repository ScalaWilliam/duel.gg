package gg.duel.pinger.data.journal

import java.io.{DataInputStream, InputStream}
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteString
import gg.duel.pinger.data.{IP, SauerBytes, SauerBytesBinary, Server}

/**
  * Created by William on 27/10/2015.
  */
trait SauerByteReader {

  protected def get(numBytes: Int): Array[Byte]

  protected def readNext: SauerBytes = {
    for {
      lengthBytes <- Option(get(2)) // get the short
      length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getShort
      data <- Option(get(length))
      if data.length == length
    } yield SauerBytesBinary.fromBytes(data)
  }.orNull

  def toIterator =
    Iterator.continually(readNext).takeWhile(_ != null)

}

case class EfficientSauerByteReader(inputStream: DataInputStream) extends SauerByteReader {
  def close(): Unit = inputStream.close()

  var BUFFER_SIZE = 1024

  private val tmpArr = Array.fill[Byte](BUFFER_SIZE)(Byte.MinValue)

  override def readNext(): SauerBytes = {
    try {
      val lengthBytes = inputStream.readShort()
      val time = inputStream.readLong()
      val ip = IP(inputStream.readInt())
      val port = inputStream.readInt()
      val cnt = lengthBytes - 8 - 4 - 4
      inputStream.read(tmpArr, 0, cnt)
      SauerBytes(
        server = Server(ip, port),
        time = time,
        message = ByteString.fromArray(tmpArr, 0, cnt)
      )
    } catch {
      case e: java.io.EOFException => null
    }
  }

  override protected def get(numBytes: Int): Array[Byte] = ???
}
