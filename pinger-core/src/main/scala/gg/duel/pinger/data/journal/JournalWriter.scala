package gg.duel.pinger.data.journal

import java.io.{FileOutputStream, File}
import java.util.zip.{Deflater, DeflaterOutputStream}

class JournalWriter(target: File) {
  def write(sauerBytes: SauerBytes): Unit = push.apply(sauerBytes)

  val theFile = new FileOutputStream(target)

  val compressedFile = new DeflaterOutputStream(theFile, new Deflater(Deflater.BEST_COMPRESSION), true)

  val push = SauerBytesWriter.createInjectedWriter{ bytes =>
    compressedFile.write(bytes)
    compressedFile.flush()
    theFile.flush()
  }
  def close(): Unit = {
    compressedFile.close()
    theFile.close()
  }

}
