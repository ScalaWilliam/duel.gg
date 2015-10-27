package gg.duel.pinger.data.journal

import java.nio.ByteOrder

import akka.util.ByteStringBuilder

trait SauerBytesWriter {

  def writeSauerBytes(sauerBytes: SauerBytes): Unit = {
    val sauerBinary =
      SauerBytesBinary
        .toBytes(sauerBytes)
        .take(Short.MaxValue)

    implicit val byteOrdering = ByteOrder.BIG_ENDIAN

    val lengthBinary =
      new ByteStringBuilder()
        .putShort(sauerBinary.length)
        .result()
        .toArray

    write(lengthBinary)
    write(sauerBinary)
  }

  def write(bytes: Array[Byte]): Unit

}









