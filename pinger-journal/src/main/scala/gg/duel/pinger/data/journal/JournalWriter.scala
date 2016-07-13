package gg.duel.pinger.data.journal

import java.io.{FileOutputStream, File}
import java.util.zip.{GZIPOutputStream, Deflater, DeflaterOutputStream}

import gg.duel.pinger.data.SauerBytes

class JournalWriter(target: File) {

  def write(sauerBytes: SauerBytes): Unit = {
    sauerBytesWriter.writeSauerBytes(sauerBytes)
  }

  val theFile = new FileOutputStream(target, true)

  val compressedFile = {
    if (target.getAbsolutePath.endsWith(".gz"))
      new GZIPOutputStream(theFile, true)
    else theFile
  }

  val sauerBytesWriter = new SauerBytesOutputStreamWriter(compressedFile)

  def close(): Unit = {
    compressedFile.flush()
    compressedFile.close()
    theFile.close()
  }

}
