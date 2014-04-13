package us.woop.pinger.analytics

import akka.actor._
import akka.actor.ActorIdentity
import akka.actor.Identify
import com.typesafe.config.ConfigFactory
import scala.Some
import us.woop.pinger.data.actor.ParsedProcessor
import us.woop.pinger.data.actor.PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.analytics.actor.{GameCollectorPublisher, BaseXPersisterActor, GlobalGameCollectorActor}
import net.xqj.basex.BaseXXQDataSource
import com.xqj2.XQConnection2
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor.HaveGame
import us.woop.pinger.analytics.actor.data.GameCollectorPublisher
import us.woop.pinger.analytics.actor.GameCollectorPublisher

object CollectGamesRemotelyApp extends App {
  val configStr =
    """
      |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
      |akka.actor.remote.enabled-transports = ["akka.remote.netty.tcp"]
      |akka.actor.remote.netty.tcp.hostname = "0.0.0.0"
      |akka.actor.remote.netty.tcp.port = 44443
      |akka.remote.secure-cookie = "C802510E1ECC5A7C18AC4DFE489CEAB231D97AAF"
      |akka.remote.require-cookie = on
      |
    """.stripMargin
  val configData = ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(configStr).withFallback(ConfigFactory.load()))
  implicit val cool = ActorSystem("Bon", configData)
  import akka.actor.ActorDSL._

  val ac = actor(new Act with ActorLogging {

    val pingerServiceSelection =
      context.actorSelection( """akka.tcp://PingerService@127.0.0.1:52552/user/pingerService/parsedProcessor""")

//    val pingerServiceSelection =
//      context.actorSelection( """akka.tcp://PingerService@188.226.161.13:52552/user/pingerService/parsedProcessor""")

    pingerServiceSelection ! Identify(None)

    val gameCollectorActor = actor(context, name = "gameCollector")(new GlobalGameCollectorActor)
    val gameCollectorPublisher = actor(context, name = "gameCollectorPublisher")(new GameCollectorPublisher)
    val persister = actor(context, name = "gameCollectorPersister")(new BaseXPersisterActor({
      val xqs = new BaseXXQDataSource() {
        setProperty("serverName", "localhost")
        setProperty("port", "1984")
        setProperty("databaseName", "dang")
      }
      xqs.getConnection("admin", "admin").asInstanceOf[XQConnection2]
    }
    ))

    gameCollectorPublisher

    become {
      case ActorIdentity(_, Some(pingerService)) =>
        pingerService.tell(ParsedProcessor.Subscribe, gameCollectorPublisher)
        gameCollectorPublisher.tell(GameCollectorPublisher.Listen, persister)
//        pingerService ! ParsedProcessor.Subscribe
        context watch pingerService
        becomeStacked {
          case m: ParsedMessage =>
            gameCollectorActor ! m
          case h: HaveGame =>
            println(s"Have game ! $h")
            persister ! h
          case Terminated(`pingerService`) =>
            context unwatch pingerService
            context.unbecome()
            pingerServiceSelection ! Identify(None)
          case any => println(s"unknown --> $any")
        }
      case ActorIdentity(_, None) =>
        pingerServiceSelection ! Identify(None)
    }

  })

}
