package us.woop.pinger

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import _root_.net.xqj.basex.BaseXXQDataSource
import akka.actor.ActorDSL._
import akka.actor._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.xqj2.XQConnection2
import us.woop.pinger.actors.{ParseRawMessagesActor, PersistReceivedBytesActor, PingPongRouterActor, PublishParsedMessagesActor}
import us.woop.pinger.data.actor.GlobalPingerClient.Monitor
import us.woop.pinger.data.actor.IndividualServerProcessor.ServerState
import us.woop.pinger.data.actor.PingPongProcessor.Server
import us.woop.pinger.data.actor.{GlobalPingerClient, ParsedProcessor}

object PingerService extends App with StrictLogging {
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
      |rb0.butchers.su
      |rb1.butchers.su
      |rb1.butchers.su 20000
      |rb2.butchers.su
      |rb3.butchers.su
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

//  val servers = "effic.me 20000".stripMargin

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

  val levelDbTarget = new File(new File("indexed-data"), dataName)

  def basexConnection = {
    val xqs = new BaseXXQDataSource() {
      setProperty("serverName", "localhost")
      setProperty("port", "1984")
      setProperty("databaseName", "matches")
    }
    xqs.getConnection("pingerpersist", "awesome").asInstanceOf[XQConnection2]
//    xqs.getConnection("admin", "admin").asInstanceOf[XQConnection2]
  }


  case class SubscriptionLink(subscriber: ActorRef, provider: ActorRef, subscribeMessage: Any)

  implicit class subscribeMe(actor: ActorRef) {
    def subscribesTo(who: ActorRef) = new {
      def via(message: Any) = SubscriptionLink(actor, who, message)
    }
  }

  system.registerOnTermination {
    logger.info("We have shut down gracefully")
  }

  val pingerService = actor(system, name = "pingerService")(new Act {


    // Monitors/Unmonitors and sends everything that's received
    val pingerClient = actor(context, name = "pingerClient")(new PingPongRouterActor)

    // Sends everything that's parsed
    val parsedProcessor = actor(context, name = "parsedProcessor")(new ParseRawMessagesActor)

    // Selectively sends everything that's parsed
    val parsedPublisher = actor(context, name = "parsedPublisher")(new PublishParsedMessagesActor)

    // Dumps raw data into a database
    val persistence = actor(context, name = "rawPersister")(new PersistReceivedBytesActor(levelDbTarget))

//    val gameCollectorPublisher = actor(context, name = "gameCollectorPublisher")(new GameCollectorPublisher)
//
//    val gameCollectorPersister = actor(context, name = "gameCollectorPersister")(new BaseXPersisterGuardianActor(basexConnection))

    def requiredLinks = Set(
      parsedProcessor        subscribesTo pingerClient via GlobalPingerClient.Listen,
      persistence            subscribesTo pingerClient via GlobalPingerClient.Listen,
      parsedPublisher        subscribesTo parsedProcessor via ParsedProcessor.Subscribe
//      gameCollectorPublisher subscribesTo parsedProcessor via ParsedProcessor.Subscribe,
//      gameCollectorPersister subscribesTo gameCollectorPublisher via GameCollectorPublisher.Listen
    )

    {
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent.duration._
      context.system.scheduler.schedule(5.seconds, 5.seconds)(enforceSubscriptions())
    }


    def enforceSubscriptions() {
      for {
        SubscriptionLink(client, server, hello) <- requiredLinks
      } {
        server.tell(hello, sender = client)
        context watch server
        context watch client
      }
    }

    whenStarting {
      enforceSubscriptions()
      for { server <- serversParsed }
        pingerClient ! Monitor(server)
    }

    become {
      case s: ServerState => println(s"Server $s")
      case any => println(s"--> $any")
    }

  })

}
