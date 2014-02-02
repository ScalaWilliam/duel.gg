import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import java.net.InetSocketAddress
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}

/** 01/02/14 */
class ClientServerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  "Pinger-Stub system" must {
    val as = ActorSystem("heloo")
    import akka.actor.ActorDSL._

    val stubActor = StubServer.makeStub(new InetSocketAddress("127.0.0.1", 1230), self)(_system)
    val SimpleUdpServer.Ready(on) = expectMsgClass(classOf[SimpleUdpServer.Ready])

    val pingerActor = _system.actorOf(Props(classOf[PingerActor], self))
    val PingerActor.Ready(how) = expectMsgClass(classOf[PingerActor.Ready])

    pingerActor ! PingerActor.Ping("127.0.0.1", on.getPort - 1)

    import scala.concurrent.duration._
    "Return at least one bad hash" in {

      val re = receiveWhile(3.seconds) {
        case x => x
      }
      println(re)
      re.collect {
        case (fromHost, PingerActor.BadHash(whence)) => true
      }.length should be > 0
    }

    "Now we test whether all responses are successful" in {
      StubServer.responser.sendBadHashes = false

      pingerActor ! PingerActor.Ping("127.0.0.1", on.getPort - 1)

      val re = receiveWhile(3.seconds) {
        case x => x
      }

      println(re)
      re.collect {
        case (fromHost, PingerActor.BadHash(whence)) => true
      } should have length 0

      info("There should be one MatchError because stub sent one bad response")
      re.collect {
        case (fromHost, PingerActor.CannotParse(why)) => true
      } should have length 1

      val output = re.groupBy {
        case (fromHost, b) => b.getClass
      }.map {
        case (clazz, items) => (clazz, items.size)
      }

      output should contain only(
        classOf[SauerbratenProtocol.ServerInfoReply] -> 1,
        classOf[SauerbratenProtocol.PlayerExtInfo] -> 5,
        classOf[SauerbratenProtocol.PlayerCns] -> 1,
        classOf[SauerbratenProtocol.TeamScores] -> 1,
        classOf[SauerbratenProtocol.HopmodUptime] -> 1,
        classOf[PingerActor.CannotParse] -> 1
        )

    }

  }
}
