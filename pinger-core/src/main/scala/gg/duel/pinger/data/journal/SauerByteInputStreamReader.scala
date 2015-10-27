package gg.duel.pinger.data.journal

import java.io.{EOFException, InputStream}
import java.nio.ByteBuffer

/**
 * Created by William on 27/10/2015.
 */
class SauerByteInputStreamReader(inputStream: InputStream) extends SauerByteReader {

  override def get(num: Int): Option[Array[Byte]] = {
    try {
      if (num == 0) return None
      require(num > 0, s"$num was < 0")
      val byteBufferArray = ByteBuffer.allocate(num).array()
      val bytesRead = inputStream.read(byteBufferArray, 0, num)
      if (bytesRead == -1) return None
      require(bytesRead == num, s"Read $bytesRead bytes, expected $num bytes")
      Option(byteBufferArray)
    } catch {
      case _: EOFException => None
    }
  }

}
