package gg.duel.pinger.data.journal

import scala.collection.mutable.ArrayBuffer

/**
 * Created by William on 27/10/2015.
 */
class SauerBytesBufferWriter extends SauerBytesWriter {

  val buffer = ArrayBuffer.empty[Byte]

  override def write(bytes: Array[Byte]): Unit = {
    buffer.append(bytes :_*)
  }

}
