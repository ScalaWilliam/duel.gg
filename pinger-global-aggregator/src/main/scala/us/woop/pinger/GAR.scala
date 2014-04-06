package us.woop.pinger

import us.woop.pinger.PingerServiceData._
import akka.actor._
import us.woop.pinger.MasterserverClientActor.RefreshServerList
import akka.actor.ActorDSL._
import scala.concurrent.duration._
import akka.event.LoggingReceive
import us.woop.pinger.PingerServiceData.Unsubscribe
import us.woop.pinger.PingerServiceData.SauerbratenPong
import scala.Some
import akka.actor.Identify

object GAR extends App {
  import com.typesafe.config.ConfigFactory
  val customConf = ConfigFactory.parseString("""
     us.woop.pinger.pinger-service.subscribe-to-ping-delay = 100ms
     us.woop.pinger.pinger-service.default-ping-interval = 500ms
   """)
  implicit val as = ActorSystem("Bonger", ConfigFactory.load(customConf))

  import scala.concurrent.ExecutionContext.Implicits.global

  val monitor = actor("monitor")(new WoopMonitor)

  val mainController = actor("hey")(new Act with ActorLogging {

    val masterserverClient: ActorRef = context.actorOf(Props[MasterserverClientActor], name = "masterserverClient")
//    val pingerServiceSelection = context.actorSelection("""akka.tcp://PingerService@188.226.161.13:2552/user/pingerService""")
    val pingerServiceSelection = context.actorOf(Props[PingerService], name="pingerService")
    val targetController: ActorRef = context.actorOf(Props[TargetControllerActor], name = "targetControllerActor")
    val dataProcessor = context.actorOf(Props[PersistenceActor], name = "dataProcessor")
    var masterserverSchedule: Option[Cancellable] = None

    whenStarting {
      pingerServiceSelection ! Identify(None)
    }

    import MasterserverClientActor._

    become {
      case ActorIdentity(_, Some(pingerService)) =>
        context watch pingerService
        log.info("Pinger service found")
        masterserverSchedule = Option(context.system.scheduler.schedule(0.seconds, 5.minutes, masterserverClient, RefreshServerList))
        become( {
          case Terminated(`pingerService`) =>
            for { mss <- masterserverSchedule } mss.cancel()
            log.error("Pinger service no longer available")
            unbecome()
          case m: MasterServers => targetController forward m
          case m: ServerGone => targetController forward m
          case m: ServerAdded => targetController forward m
          case m: SauerbratenPong => dataProcessor forward m; targetController forward m
          case m: Subscribe => pingerService ! m
          case m: Unsubscribe => pingerService ! m
        })
      case ActorIdentity(_, None) =>
        log.info("Pinger service not yet available")
        context.system.scheduler.scheduleOnce(1.second) {
          pingerServiceSelection !  Identify(None)
        }
    }
  })

}
