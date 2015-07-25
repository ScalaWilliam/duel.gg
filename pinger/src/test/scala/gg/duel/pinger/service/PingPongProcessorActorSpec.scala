package gg.duel.pinger.service

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.referencedata.SimpleUdpServer
import gg.duel.pinger.data.referencedata.SimpleUdpServer.{BadHashSauerbratenPongServer, GoodHashSauerbratenPongServer}
import gg.duel.pinger.service.PingPongProcessor.{BadHash, Ping, ReceivedBytes}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}

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

    parentedProbe(BadHashSauerbratenPongServer.props(serverBad.getInfoInetSocketAddress))
    expectMsgClass(classOf[SimpleUdpServer.Ready])

    val pingProcessor = parentedProbe(PingPongProcessorActor.props(PingPongProcessorState.empty.copy(disableHashing = false)))
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
    parentedProbe(GoodHashSauerbratenPongServer.props(server.getInfoInetSocketAddress))
    expectMsgClass(classOf[SimpleUdpServer.Ready])
    val pingProcessor = parentedProbe(PingPongProcessorActor.props(PingPongProcessorState.empty.copy(disableHashing = false)))
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
