import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/** 01/02/14 */
class PingTheWorldTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem())

  val pingerActor = _system.actorOf(Props(classOf[PingerActor], self))

  expectMsgClass(classOf[PingerActor.Ready])

  val servers = MasterserverClient.getServers(MasterserverClient.sauerMasterserver)

  for { server <- servers } pingerActor ! PingerActor.Ping(server)
  //  pingerActor ! PingerActor.Ping("81.169.137.114", 30000)



//  pingerActor ! PingerActor.Ping("85.214.66.181",10000)

  import scala.concurrent.duration._

  val output = receiveWhile(5.seconds) {
    case x => x
  }

  println(output)
//

//  val wut = List(0, 1, -1, -1, 105, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 100, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 100, 0, 0, 0, 0, -3, 1, 0, -128, -112, 1, 100, 0, 0, 0, -3, 1, 0, -128, 44, 1, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 100, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, -128, -12, 1, 0, 0, 0, 0, -3, 1, 0, -128, 44, 1, 100, 0, 0, 0, -3, 1, 0, 100, 0, 0, 0, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, -128, -56, 0, -128, -56, 0, 0, 0, 0, 0, 101, 118, 105, 108, 0, 2, 0, 0, 0, 100, 1, 0, 4, 0, 0, 89, -90, -81)
//
//
//  println(SauerbratenProtocol.matchers.lift.apply(wut.map(_.toByte)))

//  val whutOgros = List(0, 1, -1, -1, 105, 0, -11, 101, 118, 105, 108, 0, 2, 0, 0, 0, 100, 1, 0, 4, 0, 0, 89, -90, -81)
//  val input = List(0,1,-1,-1,105, 0, -11,0, 101, 118, 105, 108, 0, 2, 0, 0, 0, 100, 1, 0, 4, 0, 0, 89, -90, -81)

//  println(SauerbratenProtocol.matchers.lift.apply(input.map(_.toByte)))
//  val thmz = List(0, 1, -1, -1, 105, 0,
//    -3, 1, 0, 0, 0, 0, 0, 0,
//    -3, 1, 0, -128, 92, 18, -128, 120, 5, 0, 3, 1,
//    -3, 1, 0, -128, 24, 21, -128, -112, 1, 0, 0, 1,
//    -3, 1, 0, -128, -64, 18, -128, -68, 2, 1, 3, 1,
//    -3, 1, 0, -128, 28, 12, 100, 0, 2, 0,
//    -3, 1, 0, -128, 96, 9, -128, -112, 1, 0, 1, 1,
//    -3, 1, 0, -128, -68, 27, -128, -128, 12, 0, 1, 4,
//    -3, 1, 0, -128, 64, 31, -128, 108, 7, 0, 2, 1,
//    -3, 1, 0, -128, 124, 21, -128, -80, 4, 0, 1, 2,
//    -3, 1, 1, 0, 0, 0, 0, 0,
//    -3, 1, 0, -128, 40, 10, 100, 0, 2, 1,
//    -3, 1, 0, -128, -116, 10, -128, -12, 1, 0, 1, 1,
//    -3, 1, 0, -128, 28, 12, -128, -124, 3, 0, 1, 2,
//    -3, 1, 0, -128, 64, 6, -128, -112, 1, 1, 2, 1,
//    -3, 1, 0, -128, -112, 1, 100, 0, 0, 0,
//    0, 103, 111, 111, 100, 0, 1, 0, 2, 0, 25, 1, 0, 4, 0, 0, 87, 126, -41)
//
//   List(-1, 105, 0,
//     -11, 2, 84, 78, 101, 120, 117, 115, 0, 103, 111, 111, 100, 0, 18, 0, 16, 0, 33, 1, 0, 4, 0, 0, 46, 78, 86)
//  println(SauerbratenProtocol.matchers.lift.apply(thmz.map(_.toByte)))
}
