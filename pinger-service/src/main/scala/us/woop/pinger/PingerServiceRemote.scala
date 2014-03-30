package us.woop.pinger

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}

object PingerServiceRemote extends App {

  val config =
    """
akka {
actor {
provider = "akka.remote.RemoteActorRefProvider"
}
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "127.0.0.1"
port = 2552
}
}
}

akka.remote {
secure-cookie = "C802510E1ECC5A7C18AC4DFE489CEAB231D97AAF"
require-cookie = on
}


us.woop.pinger.pinger-service.subscribe-to-ping-delay=500ms
us.woop.pinger.pinger-service.default-ping-interval=10s
    """

  val customConf = ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(config))

  implicit val actorSystem = ActorSystem("PingerService", customConf)

  val pingerService = actorSystem.actorOf(Props(classOf[PingerService]), name="pingerService")

}
