package gg.duel.pinger.app

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import gg.duel.pinger.Pipeline
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.CompletedDuel
import gg.duel.pinger.service.WSAsyncGamePersister
import org.basex.BaseXHTTP
import com.hazelcast.config.Config
import com.hazelcast.core.{HazelcastInstance, Message, MessageListener, Hazelcast}
import org.scalatest._
import gg.duel.pinger.app.SystemIT.{HaveNewCtfId, HaveNewDuelId, HaveMeta}
import gg.duel.pinger.data.journal.{IterationMetaData, SauerBytesWriter}
import gg.duel.pinger.service.analytics.JournalBytes
import gg.duel.pinger.app.Woot._
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.referencedata.SimpleUdpServer
import gg.duel.pinger.data.referencedata.SimpleUdpServer.GoodHashSauerbratenPongServer
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes

class SystemIT (sys: ActorSystem) extends TestKit(sys) with WordSpecLike with Matchers
with ImplicitSender with  ParentedProbe with Inspectors with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  val server = Server("127.0.0.1", 12341)
  val bserver = new BaseXHTTP("-p12396", "-e12397", "-h12398", "-s12399")
  var tempHazelcastClient: HazelcastInstance = _

  val journalBuffer = collection.mutable.ArrayBuffer.empty[String]
  val ai = new AtomicInteger(0)

  var yay: ActorRef = _
  override def beforeAll() {

    val config = new Config
    config.getGroupConfig.setName("test-A")
    config.getNetworkConfig.setPort(6661)
    config.setClassLoader(this.getClass.getClassLoader)
    val tempHazelcastInstance = Hazelcast.newHazelcastInstance(config)

    val config2 = new Config
    config2.getGroupConfig.setName("test-A")
    config2.getNetworkConfig.setPort(6661)
    config2.setClassLoader(this.getClass.getClassLoader)
    tempHazelcastClient = Hazelcast.newHazelcastInstance(config2)

    import collection.JavaConverters._
//    val clientConfig = new ClientConfig
//    clientConfig.getNetworkConfig.setAddresses(List("127.0.0.1:6661").asJava)
//    clientConfig.setClassLoader(this.getClass.getClassLoader)
//    clientConfig.getGroupConfig.setName("test-A")
//    tempHazelcastClient = HazelcastClient.newHazelcastClient(clientConfig)

    "Hazelcast connectivity must work" in {
      tempHazelcastClient.getQueue[String]("yay").put("good!")
      tempHazelcastInstance.getQueue[String]("yay").poll() shouldBe "good!"
    }

    val randomDb = randomAlnum()
    val dbName = s"test-$randomDb"
    info(s"Using random db $dbName")
    val persister = new WSAsyncGamePersister(Pipeline.pipeline, "http://127.0.0.1:12398", dbName, "ngnsads")

    val jg = new JournalGenerator(imd => {
      println("Creating a writer..")
      ai.getAndIncrement
      JournalBytes.Writer(

        SauerBytesWriter.createInjectedWriter(b => {
          journalBuffer += b.toString
        }), () => ()
      )
    })

    yay = parentedProbe(Woot.props(tempHazelcastInstance, persister, jg, disableHashing = false))


    tempHazelcastClient.getTopic[String]("meta").addMessageListener(new MessageListener[String] {
      override def onMessage(message: Message[String]): Unit = {
        testActor ! HaveMeta(message.getMessageObject)
      }
    })


    tempHazelcastClient.getTopic[String]("new-duels").addMessageListener(new MessageListener[String] {
      override def onMessage(message: Message[String]): Unit = {
        testActor ! HaveNewDuelId(message.getMessageObject)
      }
    })

    tempHazelcastClient.getTopic[String]("new-ctfs").addMessageListener(new MessageListener[String] {
      override def onMessage(message: Message[String]): Unit = {
        testActor ! HaveNewCtfId(message.getMessageObject)
      }
    })

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
    bserver.stop()
  }

  def randomAlnum(length: Int = 8): String = {
    val rnd = new scala.util.Random()
    val chars = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')
    Iterator.fill(length)(rnd.nextInt(chars.size)).map(chars).mkString
  }
  "Pinger system" must {
    "Give us a meta" in {
      expectMsgType[HaveMeta]
      expectMsgType[IterationMetaData]
    }
    "Start watching a new server when it is pushed to a hazelcast" in {
      parentedProbe(GoodHashSauerbratenPongServer.props(server.getInfoInetSocketAddress))
      expectMsgType[SimpleUdpServer.Ready]
      tempHazelcastClient.getSet[String]("servers").add(s"${server.ip.ip}:${server.port}")
      val yes = expectMsgType[ReceivedBytes]
      yes.server shouldBe server
      import scala.concurrent.duration._
      val all = receiveWhile(max = 2.seconds, idle = 2.seconds) { case o => o }
      /**
       * Expect to start getting ReceivedBytes sequences
       */
    }

    "Notify when a new Meta added" in {
      val startedAt = ai.get()
      yay ! RotateMeta
      import scala.concurrent.duration._
      val meme = receiveWhile(max = 2.second, idle = 2.second) {
        case m : HaveMeta => m
        case o => o
      }
      forExactly(1, meme) { _ shouldBe a [HaveMeta] }
      val finishedAt = ai.get()
      finishedAt shouldBe (startedAt + 1)
    }

//    "Notify when a new Meta is added after a previous one" in {
//
//      /**
//       * Expect to get a notification in Hazelcast
//       * Check it's pushed into BaseX as well
//       */
////      fail()
//    }

    "Create a journal when a new meta is added and then SauerBytes are added in there" in {
      journalBuffer should not be empty
    }

    "SauerBytes converted to CompletedDuels with MetaIDs" ignore {

      /**
       * Ensure that MetaIDs are there when CompletedDuels are created
       */
      fail()
    }

    "Publish CompletedDuels into a database" in {
      val meta = IterationMetaData.build
      yay ! MetaCompletedDuel(metaId = meta, CompletedDuel.test.copy(metaId = Option(meta.id)).toSimpleCompletedDuel)
      import scala.concurrent.duration._
      val m = receiveWhile(max = 2.second, idle = 2.seconds) {
        case o => o
      }
      forExactly(1, m) {
        _ shouldBe a[NewlyAddedDuel]
      }
      forExactly(1, m) {
        _ shouldBe a[HaveNewDuelId]
      }

      /**
       * Yes
       */
    }

    "Publish CompletedCtfs into a database" in {
      val meta = IterationMetaData.build
      yay ! MetaCompletedCtf(metaId = meta, SimpleCompletedCTF.test.copy(metaId = Option(meta.id)))
      import scala.concurrent.duration._
      val m = receiveWhile(max = 2.second, idle = 2.seconds) {
        case o => o
      }
      forExactly(1, m) {
        _ shouldBe a[NewlyAddedCtf]
      }
      forExactly(1, m) {
        _ shouldBe a[HaveNewCtfId]
      }

      /**
       * Yes
       */
    }

    "Notify when a new CompletedDuel is added" ignore {
      fail()
    }
    "Notify when a new SimpleCompletedCTF is added" ignore {
      fail()
    }

  }
}
object SystemIT {

  case class HaveMeta(value: String)
  case class HaveNewDuelId(value: String)
  case class HaveNewCtfId(value: String)
}