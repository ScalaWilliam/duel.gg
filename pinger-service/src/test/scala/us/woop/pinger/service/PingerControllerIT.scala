package us.woop.pinger.service

import java.net.InetSocketAddress

import akka.actor.ActorDSL._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.referencedata.{SimpleUdpServer, StubServer}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController.Monitor

class PingerControllerIT(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  test("Pinger service integrates properly with a monitor message + stub") {

    val server = Server("127.0.0.1", 5010)

    StubServer.makeStub(new InetSocketAddress(server.ip.ip, server.port + 1), testActor)

    expectMsgClass(classOf[SimpleUdpServer.Ready])

    // create a layer for forwarding messages back
    actor(new Act {
      testActor ! context.actorOf(Props(classOf[PingerController]))
      become { case any => testActor forward any }
    })

    val pinger = expectMsgClass(classOf[ActorRef])

    expectMsg(PingerController.Ready)

    pinger ! Monitor(server)

    system.eventStream.subscribe(testActor, classOf[ReceivedBytes])

    // expect a successful response. Yay!
    expectMsgClass(classOf[ReceivedBytes])

  }
}
