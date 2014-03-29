package us.woop.pinger.testutil

import akka.actor.Actor.emptyBehavior
import akka.actor.{ActorRef, ActorSystem}
import java.net.InetSocketAddress
import scala.util.Random

/** 01/02/14 */
object StubServer extends App {


  lazy val mappings: PartialFunction[List[Int], List[List[Int]]] = {
    // server
    case 1 :: 1 :: 1 :: _ =>
      List(5, 5, -128, 3, 1, 3, -128, -61, 1, 17, 1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0) :: Nil
    // uptime
    case 0 :: 0 :: -1 :: _ =>
      List(-1, 105, -127, -31, -121, 3, 0, -2, 1, -1, 68, 101, 99, 32, 49, 53, 32, 50, 48, 49, 51, 32, 49, 56, 58, 51, 53, 58, 51, 55, 0) :: Nil
    // player stats
    case 0 :: 1 :: -1 :: _ =>
      List(-1, 105, 0, -11, 0, 33, 119, 48, 48, 112, 124, 68, 114, 46, 65, 107, 107, -121, 0, 103, 111, 111, 100, 0, 0, 0, 0, 0, 0, 100, 0, 6, 3, 5, 91, 121, -74) ::
        List(-1, 105, 0, -11, 1, 42, 107, 105, 110, 103, 0, 103, 111, 111, 100, 0, 11, 0, 11, 0, 22, 1, 0, 4, 0, 0, 5, 15, -101) ::
        List(-1, 105, 0, -11, 4, 49, 85, 101, 122, 0, 103, 111, 111, 100, 0, 9, 0, 14, 0, 20, -99, 0, 4, 0, 1, 109, 29, -77) ::
        List(-1, 105, 0, -11, 5, 30, 71, 105, 102, 116, 122, 90, 0, 103, 111, 111, 100, 0, 14, 0, 11, 0, 28, 1, 0, 4, 0, 0, 109, -64, -8) ::
        List(-1, 105, 0, -11, 2, 84, 78, 101, 120, 117, 115, 0, 103, 111, 111, 100, 0, 18, 0, 16, 0, 33, 1, 0, 4, 0, 0, 46, 78, 86) :: Nil
    // team stats
    case 0 :: 2 :: _ =>
      List(-1, 105, 1, 3, -128, -61, 1) :: List(9, 14) :: Nil
  }

  //val system = ActorSystem("hello")
  type Callback = List[Int] => Unit
  type CallbackWithData = (Callback, List[Int])
  lazy val random = new Random()
  lazy val responser = new PartialFunction[CallbackWithData, Unit] {
    var sendBadHashes = true

    def isDefinedAt(input: CallbackWithData): Boolean = {
      mappings.isDefinedAt(input._2)
    }

    def apply(input: CallbackWithData): Unit = synchronized {
      val res = mappings.apply(input._2)
      for {r <- res} {
        val data = if (sendBadHashes && random.nextBoolean()) input._2.updated(10, random.nextInt(20)) else input._2
        input._1(data ::: r)
      }
    }
  }

  def makeStub(address: InetSocketAddress, listener: ActorRef)(implicit actorSystem: ActorSystem) =
    SimpleUdpServer.udpServer2(address, listener)(responser)(emptyBehavior)(actorSystem)

  //implicit val as = ActorSystem("hey")
  //makeStub(as.actorOf(Props.empty))

}
