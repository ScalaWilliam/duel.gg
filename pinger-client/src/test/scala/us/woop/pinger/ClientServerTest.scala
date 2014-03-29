package us.woop.pinger

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import java.net.InetSocketAddress
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import us.woop.pinger.SauerbratenServerData
import us.woop.pinger.testutil.{SimpleUdpServer, StubServer}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ClientServerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  "Pinger-Stub system" must {
    val as = ActorSystem("heloo")
    import akka.actor.ActorDSL._

    val stubActor = StubServer.makeStub(new InetSocketAddress("127.0.0.1", 1230), self)(_system)
    val SimpleUdpServer.Ready(on) = expectMsgClass(classOf[SimpleUdpServer.Ready])

    val pingerActor = _system.actorOf(Props(classOf[PingerClient], self))
    val PingerClient.Ready(how) = expectMsgClass(classOf[PingerClient.Ready])

    pingerActor ! PingerClient.Ping("127.0.0.1", on.getPort - 1)

    import scala.concurrent.duration._
    "Return at least one bad hash" in {

      val re = receiveWhile(500.millis) {
        case x => x
      }
      re.collect {
        case (fromHost, PingerClient.BadHash(whence)) => true
      }.length should be > 0
    }

    "Now we test whether all responses are successful" in {
      StubServer.responser.sendBadHashes = false

      pingerActor ! PingerClient.Ping("127.0.0.1", on.getPort - 1)

      val re = receiveWhile(500.millis) {
        case x => x
      }

      re.collect {
        case (fromHost, PingerClient.BadHash(whence)) => true
      } should have length 0

      info("There should be one MatchError because stub sent one bad response")
      re.collect {
        case (fromHost, PingerClient.CannotParse(why)) => true
      } should have length 1

      val output = re.groupBy {
        case (fromHost, b) => b.getClass
      }.map {
        case (clazz, items) => (clazz, items.size)
      }

      output should contain only(
        classOf[SauerbratenServerData.ServerInfoReply] -> 1,
        classOf[SauerbratenServerData.PlayerExtInfo] -> 5,
        classOf[SauerbratenServerData.PlayerCns] -> 1,
        classOf[SauerbratenServerData.TeamScores] -> 1,
        classOf[SauerbratenServerData.HopmodUptime] -> 1,
        classOf[PingerClient.CannotParse] -> 1
      )

    }

  }
}
