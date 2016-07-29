package gg.duel.pinger.data.journal

import java.io.{DataInputStream, InputStream}
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteString
import gg.duel.pinger.data._

import scala.annotation.tailrec

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

  final override def readNext(): SauerBytes = {
    try {
      val lengthBytes = inputStream.readShort()
      inputStream.readFully(tmpArr, 0, lengthBytes)
      SauerBytes.fromArray(tmpArr, lengthBytes)
    } catch {
      case e: java.io.EOFException => null
    }
  }

  override protected def get(numBytes: Int): Array[Byte] = ???
}

object EfficientSauerByteReader {
}
