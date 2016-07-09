package gg.duel.pingerservice

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, FileInputStream}

import org.scalatest.{Matchers, WordSpec}

class ReadGameJournalSpec extends WordSpec with Matchers {
  def testGames = GamesJournalReader.fromInputStream(getClass.getResourceAsStream("/test-journal.txt"))
  "Reader" must {
    "Read 4 games" in {
      testGames.toList should have size 4
    }
    "Write 4 games in-mem and read them back" in {
      val testGamesL = testGames.toList
      val baos = new ByteArrayOutputStream()
      val gjw = new GamesJournalWriter(baos)
      testGamesL.foreach{case (id, json) => gjw.write(id, json)}
      baos.close()
      val bais = new ByteArrayInputStream(baos.toString.getBytes("UTF-8"))
      val foundGames = GamesJournalReader.fromInputStream(bais).toList
      foundGames shouldBe testGamesL
    }
    "Read 26607 games from local thing" ignore {
      def istream = new FileInputStream("../games.txt")
      val cnt = GamesJournalReader.fromInputStream(istream).size
      cnt shouldBe 26694
    }

  }
}
