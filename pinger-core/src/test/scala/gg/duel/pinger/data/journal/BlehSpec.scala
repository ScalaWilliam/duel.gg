package gg.duel.pinger.data.journal

import java.io.{FileInputStream, File}
import java.util.zip.GZIPInputStream

import org.scalatest.{Matchers, WordSpec}

/**
 * Created by William on 27/10/2015.
 */
class BlehSpec extends WordSpec with Matchers {
  val file = new File("2015-10-27T193304.sblog.gz")
  "Reader" must {
    "Read it out" in {
      val journalReader = new JournalReader(file)
      println("LENGTH = ",file.length())
//      val f = new GZIPInputStream(new FileInputStream(file))
//      val buf = Array.fill(100)(0.toByte)
//      f.read(buf, 0, 100)
//      println(buf.toList)
      try println(journalReader.getSauerBytes.toList)
      catch { case x: Throwable => println(x) }
      println("At position = ",journalReader.fis.getChannel.position())
//      val sbr = new
    }
  }
}
