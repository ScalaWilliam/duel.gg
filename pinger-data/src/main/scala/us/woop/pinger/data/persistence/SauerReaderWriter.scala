package us.woop.pinger.data.persistence

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteStringBuilder
import us.woop.pinger.data.journal.{SauerBytesBinary, SauerBytes}
import us.woop.pinger.data.persistence.SauerReaderWriter.ByteWriter

import scala.collection.mutable.ArrayBuffer

object SauerReaderWriter {

  case class ByteWriter(write: Array[Byte] => Unit, flush: () => Unit, close: () => Unit)

  val HEADER = Array[Byte](5, 4, 3, 2, 2, 2, 3, 4, 5)

  def writeToFile(file: File) = {
    val fos = new FileOutputStream(file)
    val byteWriter = ByteWriter(
      write = fos.write,
      flush = () => fos.flush(),
      close = () => fos.close()
    )
    new SauerWriter(byteWriter, true)
  }

  def writeToArrayBuffer(arrayBuffer: ArrayBuffer[Byte]) = {
    val byteWriter = ByteWriter(
      write = bytes => arrayBuffer.append(bytes: _*),
      flush = () => (),
      close = () => ()
    )
    new SauerWriter(byteWriter, true)
  }
  def readFromByteIterator(iterator: Iterator[Byte], headerExists: Boolean = true): Iterator[SauerBytes] = {

    if ( headerExists ) {
      val expectedHeader = HEADER.toVector
      val haveHeader = iterator.take(expectedHeader.size).toVector
      assert(haveHeader == expectedHeader, s"Header should be $expectedHeader, have $haveHeader")
    }

    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    Iterator.continually {
      for {
        sizeBit <- Option(iterator.take(4).toArray)
        if sizeBit.size == 4
        size = ByteBuffer.wrap(sizeBit).getInt
        bytes = iterator.take(size).toArray
        if bytes.size == size
      } yield SauerBytesBinary.fromBytes(bytes)
    }.takeWhile(_.isDefined).map(_.get)
  }
  def readFromFile(file: File) = {
    // todo do we need to close this?
    val fis = new FileInputStream(file)
    val iterator = Iterator.continually(fis.read()).takeWhile(_ != -1).map(_.toByte)
    readFromByteIterator(iterator)
  }
}

class SauerWriter(byteWriter: ByteWriter, writeHeader: Boolean = true) {

  if (writeHeader) {
    byteWriter.write(SauerReaderWriter.HEADER)
  }

  def write(sauerBytes: SauerBytes): Unit = {
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val serialized = SauerBytesBinary.toBytes(sauerBytes).take(Integer.MAX_VALUE)
    val byteArray = new ByteStringBuilder().putInt(serialized.size).putBytes(serialized).result().toArray
    byteWriter.write(byteArray)
  }

  def flush(): Unit = {
    byteWriter.flush()
  }

  def close(): Unit = {
    byteWriter.close()
  }
}
