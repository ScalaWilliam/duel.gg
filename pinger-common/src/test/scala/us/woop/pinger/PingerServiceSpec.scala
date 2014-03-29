package us.woop.pinger

import org.scalatest.{WordSpecLike, Matchers, WordSpec}
import akka.actor.{PoisonPill, Kill, Props, ActorSystem}
import akka.testkit.TestKitBase
import us.woop.pinger.PingerService.{ChangeRate, Unsubscribe, Server, Subscribe}
import us.woop.pinger.PingerClient.{Ready, Ping}

class PingerServiceSpec extends {
  implicit val system = ActorSystem("pingerServiceActorTestSystem")
} with TestKitBase with WordSpecLike with Matchers {

  import scala.concurrent.duration._
  "Pinger service actor" must {
    "Send a ping when a subscription is made" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger ! Ready
      expectMsg(Ping("abcd", 123))
      pinger ! PoisonPill
    }
    "Send only one ping when a double subscription is made" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      // also shows that stash() is working
      pinger ! Ready
      expectMsg(Ping("abcd", 123))
      expectNoMsg()
      pinger ! PoisonPill
    }
    "Send nothing when a subscription is made and cancelled" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      pinger.tell(Unsubscribe(Server("abcd", 123)), testActor)
      expectNoMsg()
      pinger ! PoisonPill
    }
    "Send a ping twice after a rate is set" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready
      pinger.tell(ChangeRate(Server("abcd", 123), 3.seconds), testActor)
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      expectMsg(Ping("abcd", 123))
      expectNoMsg(2.seconds)
      expectMsg(Ping("abcd", 123))
      expectNoMsg(2.seconds)
      pinger ! PoisonPill
    }
    "Passes a pinger client response back" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready
      pinger.tell(Subscribe(Server("abcd", 123)), testActor)
      expectMsg(Ping("abcd", 123))
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectMsg((("abcd", 123), "hello"))
      ignoreMsg{ case _ => true }
      pinger ! PoisonPill
    }
    "Does not pass a pinger client response back if not subscribed" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! Ready
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectNoMsg()
      pinger ! PoisonPill
    }
    "Does not pass a pinger client response back if not subscribed & ran with a rate change" in {
      val pinger = system.actorOf(Props(classOf[PingerService], testActor))
      pinger ! ChangeRate(Server("abcd", 123), 3.second)
      pinger ! Ready
      pinger.tell((("abcd", 123), "hello"), testActor)
      expectNoMsg()
      pinger ! PoisonPill
    }
  }

}
