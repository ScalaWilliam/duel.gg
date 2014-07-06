package us.woop.pinger.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.ParentedProbe
import us.woop.pinger.data.ParsedPongs.ServerInfoReply
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingPongProcessor.Ping
import us.woop.pinger.service.RawToExtracted.ExtractedMessage
import us.woop.pinger.service.individual.ServerMonitor.{Active, Online, ServerStateChanged}
import us.woop.pinger.service.individual.ServerSupervisor

class ServerSupervisorSpec(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe {

  def this() = this(ActorSystem())

  val server = Server("1.2.3.4", 1234)

  test("That the supervisor sends a ping straight away") {

    parentedProbe(Props(classOf[ServerSupervisor], server))

    expectMsgClass(classOf[Ping])

  }

  test("That the supervisor changes the status once given 5-people game") {

    val supervisor = parentedProbe(Props(classOf[ServerSupervisor], server))

    expectMsgClass(classOf[Ping])

    supervisor ! ExtractedMessage(
      server = server,
      time = System.currentTimeMillis,
      message = ServerInfoReply(clients = 5, 0, 0, 0, 0, None, None, "", "")
    )

    expectMsg(ServerStateChanged(server, Online(Active)))

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}
