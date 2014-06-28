package us.woop.pinger.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingPongProcessor.Ping
import us.woop.pinger.service.individual.ServerSupervisor

class ServerSupervisorSpec(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))
  
  test("That the supervisor sends a ping straight away") {

    val server = Server("1.2.3.4", 1234)

    sys.actorOf(Props(classOf[ServerSupervisor], server))

    sys.eventStream.subscribe(testActor, classOf[Ping])

    expectMsgClass(classOf[Ping])
  }

}
