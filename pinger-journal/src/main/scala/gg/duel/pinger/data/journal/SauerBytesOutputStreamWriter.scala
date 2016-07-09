package gg.duel.pinger.data.journal

import java.io.OutputStream

/**
 * Created by William on 27/10/2015.
 */
class SauerBytesOutputStreamWriter(outputStream: OutputStream) extends SauerBytesWriter {
  override def write(bytes: Array[Byte]): Unit = {
    outputStream.write(bytes)
    outputStream.flush()
  }
}
