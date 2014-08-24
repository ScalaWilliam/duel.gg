package us

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Millis, Span}
import org.scalatest.{Matchers, WordSpec}
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.data.journal.IterationMetaData


class WSAsyncDuelPersisterSpec extends WordSpec with Matchers with ScalaFutures{

implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  "Duel pusher" ignore {
    val persister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://localhost:8984", "duelsza", "yesz")

    import scala.concurrent.ExecutionContext.Implicits.global
    val sampleDuel = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)
    "List no duels after clearing db" in {
      try {
        persister.dropDatabase.futureValue
      } catch { case _ => }
      persister.createDatabase
      persister.listDuels.futureValue shouldBe empty
    }
    "Insert a duel when one does not exist and give it an ID" in {
      try {
        val result = persister.pushDuel(sampleDuel, IterationMetaData.build).futureValue
        val duel = persister.getDuel(result).futureValue
        println(duel)
        persister.listDuels.futureValue should have size 1
      } finally {

        println(persister.listDuels)
      }
    }
    "Do nothing if a duel already exists" in {
      val result = persister.pushDuel(sampleDuel, IterationMetaData.build).futureValue
      val duel = persister.getDuel(result).futureValue
      println(duel)
      persister.listDuels.futureValue should have size 1
    }
  }
}
