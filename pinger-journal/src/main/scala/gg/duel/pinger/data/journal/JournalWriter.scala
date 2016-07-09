package gg.duel.pinger.data.journal

import java.io.{FileOutputStream, File}
import java.util.zip.{GZIPOutputStream, Deflater, DeflaterOutputStream}

import gg.duel.pinger.data.SauerBytes

class JournalWriter(target: File) {

  def write(sauerBytes: SauerBytes): Unit = {
    sauerBytesWriter.writeSauerBytes(sauerBytes)
  }

  val theFile = new FileOutputStream(target)

  val compressedFile = new GZIPOutputStream(theFile, true)

  val sauerBytesWriter = new SauerBytesOutputStreamWriter(compressedFile)

  def close(): Unit = {
    compressedFile.finish()
    compressedFile.close()
    theFile.close()
  }

}
