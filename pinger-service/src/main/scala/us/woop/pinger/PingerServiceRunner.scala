package us.woop.pinger

import java.net.InetAddress
import akka.actor.{Props, Actor, ActorSystem}
import us.woop.pinger.PingerServiceData.{Unsubscribe, ChangeRate, Server, Subscribe}
import com.typesafe.config.ConfigFactory

object PingerServiceRunner extends App {

//  val woopServer = Server(InetAddress.getByName("sauer.woop.us").getHostAddress, 28785)
  val woopServer = Server("81.169.137.114", 30000)


  implicit lazy val system = {
    val customConf = ConfigFactory.parseString(
      """akka {
        |  #  loglevel = "DEBUG"
        |    actor {
        |      #  debug {
        |      #      receive = on
        |      #      autoreceive = on
        |      #      lifecycle = on
        |      #  }
        |    }
        |     loggers = ["akka.event.slf4j.Slf4jLogger"]
        |   actor {
        |     my-custom-dispatcher {
        |       mailbox-type = "akka.dispatch.UnboundedDequeBasedMailbox"
        |     }
        |   }
        |}
        |us.woop.pinger.pinger-service.subscribe-to-ping-delay = 500ms
        |us.woop.pinger.pinger-service.default-ping-interval = 30s""".
        stripMargin)

    ActorSystem("WoopDeeDoo", ConfigFactory.load(customConf))
  }

  lazy val xsystem = {
    val config = ConfigFactory.parseString(
    """us.woop.pinger.pinger-service.subscribe-to-ping-delay = 500ms
      |us.woop.pinger.pinger-service.default-ping-interval = 30s""".stripMargin
    )
    ActorSystem("WoopDeeDoo", config)
  }
  val pingerService = system.actorOf(Props(classOf[PingerService]))

  import akka.actor.ActorDSL._
  val akk = actor("hey"){new Act {
    import scala.concurrent.duration._
    println("Trololol")
    become {
      case any => println(any)
    }
    pingerService ! Subscribe(woopServer, 3.seconds)


    import context.dispatcher
    context.system.scheduler.scheduleOnce(20.seconds, pingerService, Unsubscribe(woopServer))

  }}

}
