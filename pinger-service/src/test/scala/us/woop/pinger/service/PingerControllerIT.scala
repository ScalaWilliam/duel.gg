package us.woop.pinger.service

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{Inspectors, BeforeAndAfterAll, FlatSpecLike, Matchers}
import us.woop.pinger.ParentedProbe
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.referencedata.SimpleUdpServer
import us.woop.pinger.referencedata.SimpleUdpServer.GoodHashSauerbratenPongServer
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController.Monitor
import us.woop.pinger.service.RawToExtracted.ExtractedMessage
import us.woop.pinger.service.individual.ServerMonitor.ServerStateChanged
class PingerControllerIT(sys: ActorSystem) extends TestKit(sys) with FlatSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe with Inspectors {

  def this() = this(ActorSystem())

  val server = Server("127.0.0.1", 5010)

  override def beforeAll() {
    parentedProbe(Props(classOf[GoodHashSauerbratenPongServer], new InetSocketAddress(server.ip.ip, server.port + 1)))
    expectMsgClass(classOf[SimpleUdpServer.Ready])
  }

  "Pinger service" should "integrate properly with a monitor message + stub" in {
    import concurrent.duration._

    val pingerService = parentedProbe(Props(classOf[PingerController]))
    expectMsg(PingerController.Ready)
    pingerService ! Monitor(server)
    // expect a state change
    val results = receiveN(17, 2.seconds)
    forExactly(1, results) { _ shouldBe a [ServerStateChanged] }
    forExactly(8, results) { _ shouldBe a [ReceivedBytes] }
    forExactly(8, results) { _ shouldBe a [ExtractedMessage[_]] }
    val probe = TestProbe()
    probe watch pingerService
    pingerService ! PoisonPill
    probe.expectTerminated(pingerService)
    expectNoMsg()
  }

  "Pinger service" should "send a message for an active server every three seconds" in {

    val pingerService = parentedProbe(Props(classOf[PingerController]))
    expectMsg(PingerController.Ready)
    pingerService ! Monitor(server)
    import concurrent.duration._
    val stuffs = receiveWhile(max = 2.seconds){ case x => x }
    stuffs should have size 17
    expectNoMsg(1.second)
    receiveN(16, 2.seconds)

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
