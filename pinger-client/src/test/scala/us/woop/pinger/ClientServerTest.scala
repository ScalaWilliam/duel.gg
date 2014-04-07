package us.woop.pinger

import akka.actor.{Props, ActorSystem}
import akka.testkit.{EventFilter, TestKit, ImplicitSender}
import java.net.InetSocketAddress
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import us.woop.pinger.testutil.{SimpleUdpServer, StubServer}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import us.woop.pinger.client.PingPongProcessor
import us.woop.pinger.client.PingPongProcessor.{FullPingPongProcessor, AllOutboundMessages, CannotParse, ParsedMessage}

@RunWith(classOf[JUnitRunner])
class ClientServerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  "Pinger-Stub system" must {

    val stubActor = StubServer.makeStub(new InetSocketAddress("127.0.0.1", 1230), self)(_system)
    val SimpleUdpServer.Ready(on) = expectMsgClass(classOf[SimpleUdpServer.Ready])

    import akka.actor.ActorDSL._
    val pingerActor = actor(_system)(new FullPingPongProcessor(self))
    val PingPongProcessor.Ready(how) = expectMsgClass(classOf[PingPongProcessor.Ready])

    pingerActor ! PingPongProcessor.Ping("127.0.0.1", on.getPort - 1)

    import scala.concurrent.duration._
    "Return at least one bad hash" in {

      val re = receiveWhile(1000.millis) {
        case x => x
      }
      re.collect {
        case PingPongProcessor.BadHash(_, _) => true
      }.length should be > 0
    }
    "Expect one failure when returned message sucks"
    "Now we test whether all responses are successful" in {
      StubServer.responser.sendBadHashes = false

      /** TODO catch CannotParse somehow **/
//      EventFilter[CannotParse](occurrences = 1) intercept {
        pingerActor ! PingPongProcessor.Ping("127.0.0.1", on.getPort - 1)
        val re = receiveWhile(500.millis) {
          case x => x
        }

        re.collect {
          case PingPongProcessor.BadHash(_, _) => true
        } should have length 0

        info("There should be one MatchError because stub sent one bad response")

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
          classOf[SauerbratenServerData.Conversions.ConvertedHopmodUptime] -> 1
          )
//      }

    }

  }
}
