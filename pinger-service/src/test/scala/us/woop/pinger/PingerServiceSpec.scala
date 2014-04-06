package us.woop.pinger

import org.scalatest.{WordSpecLike, Matchers, WordSpec}
import akka.actor._
import akka.testkit.TestKitBase
import us.woop.pinger.PingerServiceData._
import us.woop.pinger.PingerClient.{Ready, Ping}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import us.woop.pinger.PingerClient.Ping
import us.woop.pinger.PingerServiceData.Subscribe
import us.woop.pinger.PingerServiceData.Unsubscribe
import us.woop.pinger.PingerServiceData.ChangeRate
import us.woop.pinger.PingerClient.Ready
import com.typesafe.config.ConfigFactory
import us.woop.pinger.PingerClient.Ping
import us.woop.pinger.PingerServiceData.Unsubscribe
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.PingerClient.Ready

@RunWith(classOf[JUnitRunner])
class PingerServiceSpec extends {
  implicit val system = ActorSystem("pingerServiceActorTestSystem")
} with TestKitBase with WordSpecLike with Matchers {

  import scala.concurrent.duration._
  "Pinger service actor" must {
    "Send a ping when a subscription is made" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      expectMsg(Ping("abcd", 123))
      system.stop(pinger)
    }
    "Send only one ping when a double subscription is made" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      // also shows that stash() is working
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      expectMsg(Ping("abcd", 123))
      expectNoMsg(100.millis)
      system.stop(pinger)
    }
    "Send nothing when a subscription is made and cancelled" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger.tell(Unsubscribe(Server("abcd", 123)), testActor)
      expectNoMsg(600.millis)
      system.stop(pinger)
    }
    "Passes a pinger client response back" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      expectMsg(Ping("abcd", 123))
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectMsgClass(classOf[SauerbratenPong])
      system.stop(pinger)
    }
    "Does not pass a pinger client response back if not subscribed" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectNoMsg(200.millis)
      system.stop(pinger)
    }
    "Send a ping twice after a rate is set" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      pinger.tell(Subscribe(Server("abcd", 123), 3.seconds), testActor)
      expectMsg(Ping("abcd", 123))
      expectNoMsg(450.millis)
      expectMsg(Ping("abcd", 123))
      expectNoMsg(400.millis)
      system.stop(pinger)
      expectNoMsg(600.millis)
    }
    "Removes a subscription when an actor dies" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      import akka.actor.ActorDSL._
      val secondary = actor(new Act{become{case anything => testActor forward anything}})
      pinger.tell(Subscribe(Server("abcd", 123), 5.seconds), secondary)
      expectMsg(Ping("abcd", 123))
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectMsgClass(classOf[SauerbratenPong])
      system.stop(secondary)
      expectNoMsg(600.millis)
    }
    "Doesn't remove a schedule when one of two subscribers quits" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      import akka.actor.ActorDSL._
      val willQuit = actor(new Act{become{case anything => testActor forward anything}})
      val willNotQuit = actor(new Act{become{case anything => testActor forward anything}})
      val willQuit2 = actor(new Act{become{case anything => testActor forward anything}})
      pinger.tell(Subscribe(Server("abcd", 123)), willQuit)
      pinger.tell(Subscribe(Server("abcd", 123)), willQuit2)
      pinger.tell(Subscribe(Server("abcd", 123)), willNotQuit)
      expectMsg(Ping("abcd", 123))
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectMsgClass(classOf[SauerbratenPong])
      expectMsgClass(classOf[SauerbratenPong])
      expectMsgClass(classOf[SauerbratenPong])
      system.stop(willQuit)
      system.stop(willQuit2)
      expectMsg(Ping("abcd", 123))

      system.stop(willNotQuit)
      expectNoMsg()
    }
    "Poll at the lower of the two specified intervals" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      import akka.actor.ActorDSL._
      val lowerInterval = actor(new Act{become{ case anything => testActor forward anything}})
      val higherInterval = actor(new Act{become{ case anything => testActor forward anything}})
      pinger.tell(Subscribe(Server("abcd", 123), 1.second), lowerInterval)
      expectMsg(200.millis,Ping("abcd", 123))
      pinger.tell(Subscribe(Server("abcd", 123), 2.seconds), higherInterval)
      expectNoMsg(900.millis)
      expectMsg(Ping("abcd", 123))
      expectNoMsg(900.millis)
      expectMsg(Ping("abcd", 123))
      system.stop(lowerInterval)
      system.stop(higherInterval)
      expectNoMsg()
    }
    // TODO consider having no messages sent out straight after an unsubscribe.
    // TODO does not completely make sense in this instance.
    "Move back to polling at a longer interval when the lower interval actor leaves" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      import akka.actor.ActorDSL._
      val lowerInterval = actor(new Act{become{ case anything => testActor forward anything}})
      val higherInterval = actor(new Act{become{ case anything => testActor forward anything}})
      pinger.tell(Subscribe(Server("abcd", 123), 1.second), lowerInterval)
      expectMsg(200.millis,Ping("abcd", 123))
      pinger.tell(Subscribe(Server("abcd", 123), 5.seconds), higherInterval)
      expectNoMsg(900.millis)
      expectMsg(Ping("abcd", 123))
      system.stop(lowerInterval)
      expectMsg(200.millis, Ping("abcd", 123))
      expectNoMsg(4.seconds)
      expectMsg(Ping("abcd", 123))
      system.stop(higherInterval)
      expectNoMsg()
    }

    "Support firehose" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready(new InetSocketAddress("127.0.0.1", 0))
      import akka.actor.ActorDSL._
      val firstClient = actor(new Act{become{ case anything: SauerbratenPong => testActor ! (self, anything) }})
      val secondClient = actor(new Act{become{ case anything: SauerbratenPong => testActor ! (self, anything) }})
      val firehoseClient = actor(new Act{become{ case anything: SauerbratenPong => testActor ! (self, anything) }})
      pinger.tell(Subscribe(Server("abcd", 123), 1.second), firstClient)
      pinger.tell(Subscribe(Server("abcd", 125), 2.second), secondClient)
      pinger.tell(Firehose, firehoseClient)
      pinger.tell((("abcd", 123), "hello"), testActor)
      val (firstActor, firstPong) = expectMsgClass(classOf[(ActorRef, SauerbratenPong)])
      val (secondActor, secondPong) = expectMsgClass(classOf[(ActorRef, SauerbratenPong)])
      List(firstActor, secondActor) should contain only (firstClient, firehoseClient)
      pinger.tell((("abcd", 125), "hello"), testActor)
      val (thirdActor, thirdPong) = expectMsgClass(classOf[(ActorRef, SauerbratenPong)])
      val (fourthActor, fourthPong) = expectMsgClass(classOf[(ActorRef, SauerbratenPong)])
      List(thirdActor, fourthActor) should contain only (secondClient, firehoseClient)

      system.stop(firstClient)
      system.stop(secondClient)
      expectNoMsg()
      system.stop(firehoseClient)
      expectNoMsg()
    }

  }

}
