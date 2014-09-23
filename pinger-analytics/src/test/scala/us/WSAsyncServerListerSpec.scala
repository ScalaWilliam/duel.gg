package us

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Inside, Inspectors, Matchers, WordSpec}
import org.scalatest.time.{Millis, Second, Span}
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.data.{IP, Server}
import us.woop.pinger.data.journal.IterationMetaData

class WSAsyncServerListerSpec extends WordSpec with Matchers with ScalaFutures with Inspectors
with Inside {

  implicit val patience = PatienceConfig(timeout = scaled(Span(1, Second)), interval = scaled(Span(15, Millis)))
  val asyncDuelPersister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://localhost:8984", "duelsza", "yesz")
  val persister: AsyncDuelPersister = asyncDuelPersister
  val metaPersister: MetaPersister = asyncDuelPersister
  val serverLister: ServerRetriever = asyncDuelPersister
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
      serverLister.addServer("fuckfuck 123", "1u8y243").futureValue
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