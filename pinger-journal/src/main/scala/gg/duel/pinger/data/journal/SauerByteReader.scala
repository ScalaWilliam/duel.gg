package gg.duel.pinger.data.journal

import java.io.{DataInputStream, InputStream}
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteString
import gg.duel.pinger.data.{IP, SauerBytes, SauerBytesBinary, Server}

/**
  * Created by William on 27/10/2015.
  */
trait SauerByteReader {

  protected def get(numBytes: Int): Option[Array[Byte]]

  protected def readNext: Option[SauerBytes] = {
    for {
      lengthBytes <- get(2) // get the short
      length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getShort
      data <- get(length)
      if data.length == length
    } yield SauerBytesBinary.fromBytes(data)
  }

  def toIterator =
    Iterator.continually(readNext).takeWhile(_.isDefined).flatMap(_.toIterator)

}

case class EfficientSauerByteReader(inputStream: DataInputStream) extends SauerByteReader {
  def close(): Unit = inputStream.close()

  def calcip(a: Byte, b: Byte, c: Byte, d: Byte): String = {
    (a & 0xFF) + "." + (b & 0xFF) + "." + (c & 0xFF) + "." + (d & 0xFF)
  }

  override def readNext(): Option[SauerBytes] = {
    try {
      val lengthBytes = inputStream.readShort()
      val time = inputStream.readLong()
      val ip = IP(calcip(inputStream.read().toByte, inputStream.read().toByte, inputStream.read().toByte, inputStream.read().toByte))
      val port = inputStream.readInt()
      val cnt = lengthBytes - 8 - 4 - 4
      val arr = Array.fill[Byte](cnt)(0)
      inputStream.read(arr, 0, cnt)

      Some(SauerBytes(
        server = Server(ip, port),
        time = time,
        message = ByteString(arr)
      ))
    } catch {
      case e: java.io.EOFException => None
    }
  }

  override protected def get(numBytes: Int): Option[Array[Byte]] = ???
}
