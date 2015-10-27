package gg.duel.pinger.data.journal

import java.io.File

import gg.duel.pinger.data.Server
import org.scalatest.{Matchers, WordSpec}

class JournalReaderWriterTest extends WordSpec with Matchers {

  def sb = SauerBytes(server = Server("127.0.0.1", 1234), time = System.currentTimeMillis(), message = Vector(1,2,3,4,5))

  "Journal reader and writer" must {
    "Work together" in {
      val targetFile = File.createTempFile("temp-file-name", ".sbj")
      val jw = new JournalWriter(targetFile)
      val A = sb
      val B = A.copy(time = A.time + 1, message = Vector(45,6,2,4,1))
      jw.write(A)
      jw.write(B)
      jw.close()
      val jr = new JournalReader(targetFile)
      val sauerBytesL = jr.getSauerBytes.toList
      jr.close()
      sauerBytesL should contain only (A, B)
      targetFile.deleteOnExit()
    }
  }
}
