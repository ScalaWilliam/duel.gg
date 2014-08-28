package us.woop.pinger.app

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.Config
import com.hazelcast.core.{Message, MessageListener, Hazelcast}
import org.scalatest._
import play.api.libs.ws.WSAPI
import us.woop.pinger.data.journal.SauerBytesWriter
import us.woop.pinger.service.analytics.JournalBytes
import us.{WSAsyncDuelPersister, StandaloneWSAPI}
import us.woop.pinger.ParentedProbe
import us.woop.pinger.app.Woot.{JournalGenerator, RotateMeta}
import us.woop.pinger.data.Server
import us.woop.pinger.referencedata.SimpleUdpServer
import us.woop.pinger.referencedata.SimpleUdpServer.GoodHashSauerbratenPongServer
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController.Monitor

class SystemIT (sys: ActorSystem) extends TestKit(sys) with WordSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe with Inspectors {

  def this() = this(ActorSystem())

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "Pinger system" must {

    val config = new Config
    config.getGroupConfig.setName("test-A")
    config.getNetworkConfig.setPort(6661)
    val tempHazelcastInstance = Hazelcast.newHazelcastInstance(config)

    import collection.JavaConverters._
    val clientConfig = new ClientConfig
    clientConfig.getNetworkConfig.setAddresses(List("127.0.0.1:6661").asJava)
    clientConfig.getGroupConfig.setName("test-A")
    val tempHazelcastClient = HazelcastClient.newHazelcastClient(clientConfig)

    "Hazelcast connectivity must work" in {
      tempHazelcastClient.getQueue[String]("yay").put("good!")
      tempHazelcastInstance.getQueue[String]("yay").poll() shouldBe "good!"
    }

    val server = Server("127.0.0.1", 12341)
    val persister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://127.0.0.1:8984", "test-a", "ngnsads")

    val journalBuffer = collection.mutable.ArrayBuffer.empty[String]
    val jg = new JournalGenerator(imd => {
      JournalBytes.Writer(
        SauerBytesWriter.createInjectedWriter(b => {
          journalBuffer.append(b.toString)
        }), () => ()
      )
    })
    val yay = parentedProbe(Woot.props(tempHazelcastInstance, persister, jg))

    "Start watching a new server when it is pushed to a hazelcast" in {
      parentedProbe(GoodHashSauerbratenPongServer.props(server.getInfoInetSocketAddress))
      expectMsgType[SimpleUdpServer.Ready]
      tempHazelcastClient.getSet[String]("servers").add(s"${server.ip.ip}:${server.port}")
      val yes = expectMsgType[ReceivedBytes]
      yes.server shouldBe server
      /**
       * Expect to start getting ReceivedBytes sequences
       */
    }

    "Notify when a new Meta added" in {
      case class HaveMeta(value: String)

      tempHazelcastClient.getTopic[String]("meta").addMessageListener(new MessageListener[String] {
        override def onMessage(message: Message[String]): Unit = {
          testActor ! HaveMeta(message.getMessageObject)
        }
      })
      yay ! RotateMeta
      import scala.concurrent.duration._
      val meme = receiveWhile(max = 2.second, idle = 2.second) {
        case m : HaveMeta => m
        case o => o
      }
      forAtLeast(1, meme) { _ shouldBe a [HaveMeta] }
    }

//    "Notify when a new Meta is added after a previous one" in {
//
//      /**
//       * Expect to get a notification in Hazelcast
//       * Check it's pushed into BaseX as well
//       */
////      fail()
//    }

    "Create a journal when a new meta is added and then SauerBytes are added in there" ignore {
      Thread.sleep(500)
      journalBuffer should not be empty
      /**
       * Check that a file is created somewhere around here
       */
      fail()
    }

    "SauerBytes converted to CompletedDuels with MetaIDs" ignore {

      /**
       * Ensure that MetaIDs are there when CompletedDuels are created
       */
      fail()
    }

    "Publish CompletedDuels into a database" ignore {

      /**
       * Yes
       */
      fail()
    }

    "Notify when a new CompletedDuel is added" ignore {
      fail()
    }

  }
}
