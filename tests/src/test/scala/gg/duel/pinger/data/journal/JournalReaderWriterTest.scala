package gg.duel.pinger.data.journal

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import akka.util.ByteString
import gg.duel.pinger.data.{SauerBytes, Server}
import org.scalatest.{Matchers, WordSpec}

class JournalReaderWriterTest extends WordSpec with Matchers {

  def sb = SauerBytes(
    server = Server("127.0.0.1", 1234),
    time = System.currentTimeMillis(),
    message = ByteString((1 to 900).map(_.toByte).toArray)
  )
  val A = sb
  val B = A.copy(
    server = Server("23.41.22.2", 8821),
    time = A.time + 514123,
    message = ByteString((1 to 900).map(_.toByte).toArray ++ sb.message)
  )

  "Journal reader and writer" must {
    "Work together" in {
      val targetFile = File.createTempFile("temp-file-name", ".sbj.gz")
      targetFile.deleteOnExit()
      val jw = new JournalWriter(targetFile)
      //      jw.write(A)
      for {_ <- 1 to 1000} jw.write(B)
      //      jw.write(B)
      jw.close()
      val jr = new JournalReader(targetFile)
      val sauerBytesL = jr.getSauerBytes.toList

      jr.close()
      sauerBytesL should contain only B
    }
  }

  "Buffer reader and writer" must {
    "Work together" in {
      val bw = new SauerBytesBufferWriter()
      for { _ <- 1 to 1000 } bw.writeSauerBytes(B)
      val br = new SauerByteArrayReader(bw.buffer.toArray)
      br.toIterator.toList should have size 1000
    }
  }

  "GZIP Writer and Reader" must {
    "Work together" in {
      val targetFile = File.createTempFile("temp-file-name", ".sbj")
      targetFile.deleteOnExit()
      val stuff = (-5000 to 5000).map(_.toByte).toArray
      val theFile = new FileOutputStream(targetFile)
      val compressedFile = new GZIPOutputStream(theFile, true)
      compressedFile.write(stuff)
      compressedFile.close()
      theFile.close()
      val fis = new FileInputStream(targetFile)
      val reader = new GZIPInputStream(fis)
      val tmp = Array.fill(10001)(0.toByte)
      reader.read(tmp, 0, 10001)
      tmp.toList shouldBe stuff.toList
    }
  }

}
