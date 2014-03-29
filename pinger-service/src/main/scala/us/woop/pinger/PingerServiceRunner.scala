package us.woop.pinger

import java.net.InetAddress
import akka.actor.{Props, Actor, ActorSystem}
import us.woop.pinger.PingerServiceData.{Unsubscribe, ChangeRate, Server, Subscribe}
import com.typesafe.config.ConfigFactory

object PingerServiceRunner extends App {

  val woopServer = Server(InetAddress.getByName("sauer.woop.us").getHostAddress, 28785)

  implicit val system = ActorSystem("WoopDeeDoo")
  val pingerService = system.actorOf(Props(classOf[PingerService]))

  import akka.actor.ActorDSL._
  val akk = actor("hey"){new Act {
    println("Trololol")
    become {
      case any => println(any)
    }
    pingerService ! Subscribe(woopServer)

    import scala.concurrent.duration._
    pingerService ! ChangeRate(woopServer, 5.seconds)

    import context.dispatcher
    context.system.scheduler.scheduleOnce(20.seconds, pingerService, Unsubscribe(woopServer))

  }}

  lazy val debuggedSystem = {
    val customConf = ConfigFactory.parseString(
      """akka {
        |    loglevel = "DEBUG"
        |    actor {
        |        debug {
        |            receive = on
        |            autoreceive = on
        |            lifecycle = on
        |        }
        |    }
        |     loggers = ["akka.event.slf4j.Slf4jLogger"]
        |    loglevel = "DEBUG"
        |   actor {
        |     my-custom-dispatcher {
        |       mailbox-type = "akka.dispatch.UnboundedDequeBasedMailbox"
        |     }
        |   }
        |}""".
        stripMargin)

    ActorSystem("WoopDeeDoo", ConfigFactory.load(customConf))
  }

}
