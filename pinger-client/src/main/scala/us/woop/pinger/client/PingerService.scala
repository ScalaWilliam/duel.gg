package us.woop.pinger.client

import akka.actor.ActorSystem
import akka.actor.ActorDSL._
import us.woop.pinger.client.data.{PingPongProcessor, ParsedProcessor, MasterserverClientActor, GlobalPingerClient}
import GlobalPingerClient.{Listen, Monitor}
import PingPongProcessor.Server
import java.io.File
import com.typesafe.config.ConfigFactory
import MasterserverClientActor.RefreshServerList

object PingerService extends App {
  val configStr =
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
    secure-cookie = "C802510E1ECC5A7C18AC4DFE489CEAB231D97AAF"
    require-cookie = on
  }
}
"""

  val system = ActorSystem("PingerService", ConfigFactory.parseString(configStr).withFallback(ConfigFactory.load()))

  val pingerService = actor(system, name = "pingerService")(new Act {

    // Monitors/Unmonitors and sends everything that's received
    val pingerClient = actor(context, name = "pingerClient")(new GlobalPingerClient)

    // Sends everything that's parsed!
    val parsedProcessor = actor(context, name = "parsedProcessor")(new ParsedProcessor)

    // Selectively sends everything that's parsed
    val parsedSubscriber = actor(context, name = "parsedSubscrber")(new ParsedSubscriber)

    // Dumps raw data into a database
    val persistence = actor(context, name = "persister")(new PersistRawData(new File("./sample-data")))

    // Masterserver client
    val masterserver = actor(context, name = "masterserverClient")(new MasterserverClientActor)

    whenStarting {
      import scala.concurrent.ExecutionContext.Implicits.global
      import concurrent.duration._
      context.system.scheduler.schedule(1.second, 5.minutes, masterserver, RefreshServerList)
      pingerClient.tell(GlobalPingerClient.Listen, parsedProcessor)
      pingerClient.tell(GlobalPingerClient.Listen, persistence)
      parsedProcessor.tell(ParsedProcessor.Subscribe, parsedSubscriber)
    }

    import MasterserverClientActor._

    become {

      case MasterServers(servers) =>
        for { (ip, port) <- servers }
          pingerClient ! Monitor(Server(ip, port))

      case ServerAdded((ip, port)) =>
        pingerClient ! Monitor(Server(ip, port))

      case ServerGone(_) =>

    }

  })

}
