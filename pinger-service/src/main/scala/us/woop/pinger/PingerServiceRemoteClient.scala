package us.woop.pinger

import us.woop.pinger.PingerServiceData.{ChangeRate, Subscribe, Server}
import akka.actor._
import com.typesafe.config.ConfigFactory
import us.woop.pinger.PingerServiceData.Subscribe
import akka.actor.Terminated
import us.woop.pinger.PingerServiceData.ChangeRate
import scala.Some
import akka.actor.Identify
import akka.event.LoggingReceive

object PingerServiceRemoteClient extends App {

  case class Hostname(value: String)
  import java.net.InetAddress.getByName
//  val woopServer = Server("81.169.137.114", 30000)
  val woopServer = Server(getByName("sauer.woop.us"))
  val config = ConfigFactory.parseString(
    """
      |
      |
      |    akka {
      |    actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |    }
      |    remote {
      |    enabled-transports = ["akka.remote.netty.tcp"]
      |    netty.tcp {
      |    hostname = "127.0.0.1"
      |    port = 0
      |    }
      |    }
      |    }
      |    akka {
      |    #loglevel = "DEBUG"
      |    actor {
      |    #    debug {
      |    #        receive = on
      |    #        autoreceive = on
      |    #        lifecycle = on
      |    #    }
      |    }
      |     loggers = ["akka.event.slf4j.Slf4jLogger"]
      |   actor {
      |     my-custom-dispatcher {
      |       mailbox-type = "akka.dispatch.UnboundedDequeBasedMailbox"
      |     }
      |   }
      |}
      |
      |
    """.stripMargin
  )
  implicit val cool = ActorSystem("Bon",config)
  import akka.actor.ActorDSL._

  val ac = actor(new Act{
    val pingerServiceSelection =
      context.actorSelection("""akka.tcp://PingerService@127.0.0.1:2552/user/pingerService""")
    pingerServiceSelection ! Identify(None)
    import scala.concurrent.duration._
    become {
      case ActorIdentity(_, Some(pingerService)) =>
        context watch pingerService
        pingerService ! Subscribe(woopServer, 3.seconds)
        become {
          case Terminated(`pingerService`) =>
            throw new RuntimeException("Pinger service has disappeared!")
          case other =>
            println(s"woohoo = $other")
        }
      case ActorIdentity(_, None) =>
        pingerServiceSelection ! Identify(None)
    }


  })


}
