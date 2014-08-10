package us.woop.pinger.service

import java.net.InetSocketAddress

import akka.actor.{PoisonPill, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.ParentedProbe
import us.woop.pinger.data.Server
import us.woop.pinger.referencedata.SimpleUdpServer
import us.woop.pinger.referencedata.SimpleUdpServer.{BadHashSauerbratenPongServer, GoodHashSauerbratenPongServer}
import us.woop.pinger.service.PingPongProcessor.{BadHash, Ping, ReceivedBytes}

class PingPongProcessorActorSpec(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe {

  def this() = this(ActorSystem())

  val server = Server("127.0.0.1", 5010)
  val serverBad = Server("127.0.0.1", 5015)

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  test("That PPPA sends a bad hash message when a bad hash is given by the server") {

    parentedProbe(Props(classOf[BadHashSauerbratenPongServer], serverBad.getInfoInetSocketAddress))
    expectMsgClass(classOf[SimpleUdpServer.Ready])

    val pingProcessor = parentedProbe(Props(classOf[PingPongProcessorActor]))
    expectMsgClass(classOf[PingPongProcessor.Ready])
    pingProcessor ! Ping(serverBad)
    receiveWhile(messages = 5 + 2 + 1) {
      case x: BadHash => x
    }
    expectNoMsg()
    pingProcessor ! PoisonPill
    expectNoMsg()
  }

  test("That PPPA sends a ReceivedMessage when a good message is given by the server") {
    parentedProbe(Props(classOf[GoodHashSauerbratenPongServer], server.getInfoInetSocketAddress))
    expectMsgClass(classOf[SimpleUdpServer.Ready])
    val pingProcessor = parentedProbe(Props(classOf[PingPongProcessorActor]))
    expectMsgClass(classOf[PingPongProcessor.Ready])
    pingProcessor ! Ping(server)
    receiveWhile(messages = 5 + 2 + 1) {
      case x: ReceivedBytes => x
    }
    expectNoMsg()
    pingProcessor ! PoisonPill
    expectNoMsg()
  }

}
