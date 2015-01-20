package gg.duel.pinger.service

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import gg.duel.pinger.app.ParentedProbe
import org.scalatest.{Inspectors, BeforeAndAfterAll, FlatSpecLike, Matchers}
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.referencedata.SimpleUdpServer
import gg.duel.pinger.data.referencedata.SimpleUdpServer.GoodHashSauerbratenPongServer
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes
import gg.duel.pinger.service.PingerController.Monitor
import gg.duel.pinger.service.RawToExtracted.ExtractedMessage
import gg.duel.pinger.service.individual.ServerMonitor.ServerStateChanged
class PingerControllerIT(sys: ActorSystem) extends TestKit(sys) with FlatSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe with Inspectors {
  def this() = this(ActorSystem())
  val server = Server("127.0.0.1", 5010)

  override def beforeAll() {
    parentedProbe(Props(classOf[GoodHashSauerbratenPongServer], server.getInfoInetSocketAddress))
    expectMsgClass(classOf[SimpleUdpServer.Ready])
  }

  "Pinger service" should "integrate properly with a monitor message + stub" in {
    import concurrent.duration._
    val pingerService = parentedProbe(PingerController.props(disableHashing = false))
    expectMsg(PingerController.Ready)
    pingerService ! Monitor(server)
    // expect a state change
    val results = receiveN(25, 2.seconds)
    forExactly(1, results) { _ shouldBe a [ServerStateChanged] }
    forExactly(8, results) { _ shouldBe a [ReceivedBytes] }
    forExactly(8, results) { _ shouldBe a [ParsedMessage] }
    forExactly(8, results) { _ shouldBe a [ExtractedMessage[_]] }
    val probe = TestProbe()
    probe watch pingerService
    pingerService ! PoisonPill
    probe.expectTerminated(pingerService)
    expectNoMsg()
  }

  "Pinger service" should "send a message for an active server every three seconds" in {

    val pingerService = parentedProbe(PingerController.props(disableHashing = false))
    expectMsg(PingerController.Ready)
    pingerService ! Monitor(server)
    import concurrent.duration._
    val stuffs = receiveWhile(max = 2.seconds){ case x => x }
    stuffs should have size 25
    expectNoMsg(1.second)
    receiveN(24, 2.seconds)

    val probe = TestProbe()
    probe watch pingerService
    pingerService ! PoisonPill
    probe.expectTerminated(pingerService)

    expectNoMsg()

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}
