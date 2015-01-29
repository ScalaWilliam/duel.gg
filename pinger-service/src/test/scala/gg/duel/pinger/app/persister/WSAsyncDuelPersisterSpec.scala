package us

import akka.actor.ActorSystem
import gg.duel.pinger.Pipeline
import gg.duel.pinger.analytics.duel.CompletedDuel
import gg.duel.pinger.service._
import org.basex.BaseXHTTP
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Millis, Span}
import org.scalatest._
import BaseXPersister.MetaId
import gg.duel.pinger.data.journal.IterationMetaData

import scala.util.control.NonFatal

class WSAsyncDuelPersisterSpec extends WordSpec with Matchers with ScalaFutures with Inspectors with BeforeAndAfterAll
with Inside {

implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  implicit val testSystem = ActorSystem("Test")
  val asyncDuelPersister = new WSAsyncGamePersister(Pipeline.pipeline, "http://localhost:12398", "duelsza", "yesz")
  val persister: AsyncDuelPersister = asyncDuelPersister
  val metaPersister: MetaPersister = asyncDuelPersister
  val serverLister: ServerRetriever = asyncDuelPersister
  import scala.concurrent.ExecutionContext.Implicits.global
  val sampleDuel = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)
  var server:BaseXHTTP = _
  override def beforeAll(): Unit = {
    server = new BaseXHTTP("-p12396", "-e12397", "-h12398", "-s12399")
    try {
      asyncDuelPersister.dropDatabase.futureValue
    } catch { case NonFatal(_) => }
    println(asyncDuelPersister.createDatabase.futureValue)
  }
  override def afterAll(): Unit = {
    server.stop()
    testSystem.shutdown()
    testSystem.awaitTermination()
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
