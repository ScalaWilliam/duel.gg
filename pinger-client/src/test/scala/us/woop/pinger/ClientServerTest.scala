package us.woop.pinger

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestKit, ImplicitSender}
import java.net.InetSocketAddress
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import us.woop.pinger.testutil.{SimpleUdpServer, StubServer}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import us.woop.pinger.PingerClient.{CannotParse, ParsedMessage}

@RunWith(classOf[JUnitRunner])
class ClientServerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  "Pinger-Stub system" must {
    val as = ActorSystem("heloo")

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
        case PingerClient.BadHash(_, _) => true
      }.length should be > 0
    }

    "Now we test whether all responses are successful" in {
      StubServer.responser.sendBadHashes = false

      pingerActor ! PingerClient.Ping("127.0.0.1", on.getPort - 1)

      val re = receiveWhile(500.millis) {
        case x => x
      }

      re.collect {
        case PingerClient.BadHash(_, _) => true
      } should have length 0

      info("There should be one MatchError because stub sent one bad response")
      val woo = re.collect {
        case m @ PingerClient.CannotParse(_, _) => m
      }
      woo should have length 1

      val output = re.collect {
        case ParsedMessage(_, _, b) => b.getClass
        case a: CannotParse => a.getClass
      }.groupBy(identity).map {
        case (clazz, items) => (clazz, items.size)
      }

      output should contain only(
        classOf[SauerbratenServerData.ServerInfoReply] -> 1,
        classOf[SauerbratenServerData.Conversions.ConvertedServerInfoReply] -> 1,
        classOf[SauerbratenServerData.PlayerExtInfo] -> 5,
        classOf[SauerbratenServerData.PlayerCns] -> 1,
        classOf[SauerbratenServerData.TeamScores] -> 1,
        classOf[SauerbratenServerData.Conversions.ConvertedTeamScore] -> 1,
        classOf[SauerbratenServerData.HopmodUptime] -> 1,
        classOf[SauerbratenServerData.Conversions.ConvertedHopmodUptime] -> 1,
        classOf[PingerClient.CannotParse] -> 1
      )

    }

  }
}
