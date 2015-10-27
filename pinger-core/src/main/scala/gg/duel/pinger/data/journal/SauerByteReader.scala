package gg.duel.pinger.data.journal

import java.nio.{ByteOrder, ByteBuffer}

/**
 * Created by William on 27/10/2015.
 */
trait SauerByteReader {

  def get(numBytes: Int): Option[Array[Byte]]

  def readNext: Option[SauerBytes] = {
    for {
      lengthBytes <- get(2) // get the short
      length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getShort
      data <- get(length)
      if data.length == length
    } yield SauerBytesBinary.fromBytes(data)
  }

  def toIterator =
    Iterator.continually(readNext).takeWhile(_.isDefined).flatten

}
