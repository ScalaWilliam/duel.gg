package us.woop.pinger.service

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.client.PingPongProcessor.{ReceivedBytes, Server}
import us.woop.pinger.data.actor.GlobalPingerClient.Monitor
import us.woop.pinger.referencedata.{SimpleUdpServer, StubServer}
import akka.actor.ActorDSL._

import scala.collection.mutable

class PingerControllerIT(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  test("Pinger service") {

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
