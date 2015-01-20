package us

import akka.actor.ActorSystem
import gg.duel.pinger.Pipeline
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.service._
import org.basex.BaseXHTTP
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Second, Span}
import org.scalatest._
import BaseXPersister.MetaId
import gg.duel.pinger.data.journal.IterationMetaData
import scala.util.control.NonFatal

class WSAsyncCtfPersisterSpec extends WordSpec with Matchers with ScalaFutures with Inspectors with BeforeAndAfterAll
with Inside {

  implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  implicit val testSystem = ActorSystem("Test")
  val asyncCtfPersister = new WSAsyncGamePersister(Pipeline.pipeline, "http://localhost:12398", "ctfsza", "yesz")
  val persister: AsyncCtfPersister = asyncCtfPersister
  val metaPersister: MetaPersister = asyncCtfPersister
  val serverLister: ServerRetriever = asyncCtfPersister
  import scala.concurrent.ExecutionContext.Implicits.global
  val sampleCtf = SimpleCompletedCTF.test.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)

  val server = new BaseXHTTP("-p12396", "-e12397", "-h12398", "-s12399")
  override def afterAll(): Unit = {
    server.stop()
    testSystem.shutdown()
    testSystem.awaitTermination()
  }

  "Basex client for ctf pusher" must {
    "Recreate the database" in {
      try {
        asyncCtfPersister.dropDatabase.futureValue
      } catch { case NonFatal(_) => }
      asyncCtfPersister.createDatabase
    }
  }

  "Ctf pusher" must {

    "List no ctfs after clearing db" in {
      persister.listCtfs.futureValue shouldBe empty
    }

    "Insert a ctf when one does not exist and give it an ID" in {
      persister.listCtfs.futureValue should have size 0
      persister.pushCtf(sampleCtf).futureValue
      persister.listCtfs.futureValue should have size 1

      val ctf = persister.getSimilarCtf(sampleCtf).futureValue
      info(s"Received ctf: $ctf")
    }
    "Do not insert a ctf if one exists and return a similar one" in {

      val xml = sampleCtf.toXml
      val dbName = asyncCtfPersister.dbName
      val chars = asyncCtfPersister.chars
      val previousSize = persister.listCtfs.futureValue.size
      val result = persister.pushCtf(sampleCtf).futureValue
      persister.listCtfs.futureValue.size shouldBe 1

      val ctf = persister.getSimilarCtf(sampleCtf).futureValue
      info(s"Received ctf: $ctf")
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
