package us.woop.pinger.service

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSuiteLike}
import akka.actor.ActorDSL._
import us.woop.pinger.client.PingPongProcessor.{Ping, Server}

class ServerSupervisorSpec(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))
  
  test("Wut") {

    val server = Server("1.2.3.4", 1234)

    sys.actorOf(Props(classOf[ServerSupervisor], server))

    sys.eventStream.subscribe(testActor, classOf[Ping])

    expectMsgClass(classOf[Ping])
  }

}
