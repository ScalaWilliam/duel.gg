package us.woop.pinger

import akka.actor.ActorSystem
import java.net.InetAddress
import us.woop.pinger.client.ServerPinger

object ServerPingerTestRun extends App {
  implicit val sys = ActorSystem("bang")
  val addr = (InetAddress.getByName("sauer.woop.us").getHostAddress, PingerServiceData.defaultSauerbratenPort)
  sys.actorOf(props = ServerPinger.buildStandard(addr), name = "mainBinger")
}
class ServerPingerTest {

}
