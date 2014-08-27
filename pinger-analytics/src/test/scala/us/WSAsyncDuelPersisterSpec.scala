package us

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Millis, Span}
import org.scalatest.{Matchers, WordSpec}
import us.BaseXPersister.MetaId
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.data.journal.IterationMetaData

class WSAsyncDuelPersisterSpec extends WordSpec with Matchers with ScalaFutures{

implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  val asyncDuelPersister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://localhost:8984", "duelsza", "yesz")
  val persister: AsyncDuelPersister = asyncDuelPersister
  val metaPersister: MetaPersister = asyncDuelPersister
  import scala.concurrent.ExecutionContext.Implicits.global
  val sampleDuel = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)

  "Basex client" must {
    "Recreate the database" in {
      try {
        asyncDuelPersister.dropDatabase.futureValue
      } catch { case _ => }
      asyncDuelPersister.createDatabase
    }
  }

  "Duel pusher" must {

    "List no duels after clearing db" in {
      persister.listDuels.futureValue shouldBe empty
    }

    "Insert a duel when one does not exist and give it an ID" in {
      persister.listDuels.futureValue should have size 0
      persister.pushDuel(sampleDuel).futureValue
      persister.listDuels.futureValue should have size 1

      val duel = persister.getSimilarDuel(sampleDuel).futureValue
      info(s"Received duel: $duel")
    }
    "Do not insert a duel if one exists and return a similar one" in {
      val previousSize = persister.listDuels.futureValue.size
      val result = persister.pushDuel(sampleDuel).futureValue
      persister.listDuels.futureValue.size shouldBe previousSize

      val duel = persister.getSimilarDuel(sampleDuel).futureValue
      info(s"Received duel: $duel")
    }
  }

  "Meta pusher" must {
    "List no metas after clearing db" in {
      metaPersister.listMetas.futureValue shouldBe empty
    }
    val meta = IterationMetaData.build
    "Insert a meta" in {
      metaPersister.pushMeta(meta).futureValue
      metaPersister.listMetas.futureValue should have size 1
      metaPersister.getMeta(MetaId(meta.id)).futureValue should not be 'empty
      metaPersister.listMetas.futureValue should have size 1
    }
    "Not insert a new meta" in {
      metaPersister.pushMeta(meta).futureValue
      metaPersister.listMetas.futureValue should have size 1
    }
  }
}
