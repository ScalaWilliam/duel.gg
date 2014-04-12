package us.woop.pinger

import akka.actor.ActorSystem
import akka.actor.ActorDSL._
import us.woop.pinger.data.actor.{ParsedProcessor, PingPongProcessor, GlobalPingerClient}
import GlobalPingerClient.Monitor
import PingPongProcessor.Server
import java.io.File
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import java.util.Date
import java.text.SimpleDateFormat
import us.woop.pinger.actors.{PublishParsedMessagesActor, PingPongRouterActor, PersistReceivedBytesActor, ParseRawMessagesActor}
import us.woop.pinger.data.actor.IndividualServerProcessor.ServerState

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

  val servers =
    """
      |vaq-clan.de
      |effic.me 10000
      |effic.me 20000
      |effic.me 30000
      |effic.me 40000
      |effic.me 50000
      |effic.me 60000
      |psl.sauerleague.org 20000
      |psl.sauerleague.org 30000
      |psl.sauerleague.org 40000
      |psl.sauerleague.org 50000
      |psl.sauerleague.org 60000
      |sauer.woop.us
      |vaq-clan.de
      |butchers.su
      |noviteam.de
      |darkkeepers.dk 28786
    """.stripMargin

  val noPort = """^([^ ]+)$""".r
  val withPort = """^([^ ]+) ([0-9]+)$""".r
  val serversParsed = servers.split("\n").map{_.trim}.filterNot{_ == ""}.collect {
    case withPort(host, port) => Server(host, port.toInt)
    case noPort(host) => Server(host)
    case x => throw new RuntimeException(s"cannot parse '$x'")
  }
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
    val pingerClient = actor(context, name = "pingerClient")(new PingPongRouterActor)

    // Sends everything that's parsed
    val parsedProcessor = actor(context, name = "parsedProcessor")(new ParseRawMessagesActor)

    // Selectively sends everything that's parsed
    val parsedSubscriber = actor(context, name = "parsedSubscrber")(new PublishParsedMessagesActor)

    // Dumps raw data into a database
    val persistence = actor(context, name = "persister")(new PersistReceivedBytesActor(target))

    whenStarting {
      import scala.concurrent.ExecutionContext.Implicits.global
      import concurrent.duration._
      pingerClient.tell(GlobalPingerClient.Listen, parsedProcessor)
      pingerClient.tell(GlobalPingerClient.Listen, persistence)
      parsedProcessor.tell(ParsedProcessor.Subscribe, parsedSubscriber)
      for { server <- serversParsed }
        pingerClient ! Monitor(server)
    }


    become {
      case s: ServerState => println(s"Server ")
      case any => println(s"--> $any")
      case _ =>
    }

  })

}
