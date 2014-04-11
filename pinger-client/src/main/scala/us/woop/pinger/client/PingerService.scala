package us.woop.pinger.client

import akka.actor.ActorSystem
import akka.actor.ActorDSL._
import us.woop.pinger.data.{PingPongProcessor, ParsedProcessor, MasterserverClientActor, GlobalPingerClient}
import GlobalPingerClient.Monitor
import PingPongProcessor.Server
import java.io.File
import com.typesafe.config.ConfigFactory
import MasterserverClientActor.RefreshServerList
import com.typesafe.scalalogging.slf4j.Logging
import java.util.Date
import java.text.SimpleDateFormat

object PingerService extends App with Logging {
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
      port = 52552
    }
    secure-cookie = "C802510E1ECC5A7C18AC4DFE489CEAB231D97AAF"
    require-cookie = on
  }
}
"""

  val configData = ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(configStr).withFallback(ConfigFactory.load()))

  val system = ActorSystem("PingerService", configData)


  val dataName = {
    val df = new SimpleDateFormat("dd-HH")
    df.format(new Date())
  }

  val target = new File(new File("indexed-data"), dataName)

  system.registerOnTermination {
    logger.info("We have shut down gracefully")
  }

  val pingerService = actor(system, name = "pingerService")(new Act {

    // Monitors/Unmonitors and sends everything that's received
    val pingerClient = actor(context, name = "pingerClient")(new GlobalPingerClient)

    // Sends everything that's parsed
    val parsedProcessor = actor(context, name = "parsedProcessor")(new ParsedProcessor)

    // Selectively sends everything that's parsed
    val parsedSubscriber = actor(context, name = "parsedSubscrber")(new ParsedSubscriber)

    // Dumps raw data into a database
    val persistence = actor(context, name = "persister")(new PersistRawData(target))

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
