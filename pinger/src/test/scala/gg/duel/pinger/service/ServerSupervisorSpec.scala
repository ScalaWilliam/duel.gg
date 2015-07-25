package gg.duel.pinger.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import gg.duel.pinger.data.Server
import gg.duel.pinger.service.ParentedProbe
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import gg.duel.pinger.data.ParsedPongs.ServerInfoReply
import gg.duel.pinger.data.Server
import gg.duel.pinger.service.PingPongProcessor.Ping
import gg.duel.pinger.service.RawToExtracted.ExtractedMessage
import gg.duel.pinger.service.individual.ServerMonitor.{Active, Online, ServerStateChanged}
import gg.duel.pinger.service.individual.ServerSupervisor

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
