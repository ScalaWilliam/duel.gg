package us

import gg.duel.pinger.analytics.duel.CompletedDuel
import gg.duel.pinger.service._
import org.basex.BaseXHTTP
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import org.scalatest.time.{Seconds, Millis, Second, Span}
import gg.duel.pinger.data.{IP, Server}
import gg.duel.pinger.data.journal.IterationMetaData

import scala.util.control.NonFatal

class WSAsyncServerListerSpec extends WordSpec with Matchers with ScalaFutures with Inspectors with BeforeAndAfterAll
with Inside {

  implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  val asyncDuelPersister = new WSAsyncGamePersister(new StandaloneWSAPI, "http://localhost:12398", "duelsza", "yesz")
  val persister: AsyncDuelPersister = asyncDuelPersister
  val metaPersister: MetaPersister = asyncDuelPersister
  val serverLister: ServerRetriever = asyncDuelPersister
  import scala.concurrent.ExecutionContext.Implicits.global
  val sampleDuel = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)
  val server = new BaseXHTTP("-p12396", "-e12397", "-h12398", "-s12399")
  override def afterAll(): Unit = {
    server.stop()
  }
  "Basex client" must {
    "Recreate the database" in {
      try {
        asyncDuelPersister.dropDatabase.futureValue
      } catch { case NonFatal(_) => }
      asyncDuelPersister.createDatabase
    }
  }

  "Servers lister" must {
    "List no servers after clearing db" in {
      serverLister.retrieveServers.futureValue.active shouldBe empty
      serverLister.retrieveServers.futureValue.inactive shouldBe empty
    }
    "List a server once after adding it" in {
      serverLister.addServer("localhost 1231", "L1231").futureValue
      serverLister.retrieveServers.futureValue.active should have size 1
      serverLister.retrieveServers.futureValue.inactive should have size 0
    }
    "List a server once after adding another copy of itself" in {
      serverLister.addServer("localhost 1231", "L1231").futureValue
      serverLister.retrieveServers.futureValue.active should have size 1
      serverLister.retrieveServers.futureValue.inactive should have size 0
    }
    "Not list a server that does not resolve" in {
      serverLister.addServer("fuckfuck 123", "1u8y243").futureValue(timeout = Timeout(Span(10, Seconds)))
      serverLister.retrieveServers.futureValue.active should have size 1
      serverLister.retrieveServers.futureValue.inactive should have size 0
    }
    "List two servers after adding a copy" in {
      serverLister.addServer("localhost 1232", "L1232").futureValue
      serverLister.retrieveServers.futureValue.active should have size 2
      serverLister.retrieveServers.futureValue.inactive should have size 0
    }
    "List one active, one inactive after deactivating one" in {
      serverLister.deactivateServer("localhost 1232").futureValue
      serverLister.retrieveServers.futureValue.active should have size 1
      serverLister.retrieveServers.futureValue.inactive should have size 1
    }
    "List two actives after activating one" in {
      serverLister.activateServer("localhost 1232").futureValue
      serverLister.retrieveServers.futureValue.active should have size 2
      serverLister.retrieveServers.futureValue.inactive should have size 0
    }
    "Change alias appropriately" in {
      serverLister.changeServerAlias("localhost 1232", "L312").futureValue
      forExactly(1, serverLister.retrieveServers.futureValue.active) {
        server =>
          server.active shouldBe true
          server.alias shouldBe "L312"
          server.connect shouldBe "localhost 1232"
          inside(server.server) {
            case Server(IP(ip), port) =>
              ip shouldBe "127.0.0.1"
              port shouldBe 1232
          }
      }
    }
  }
}
