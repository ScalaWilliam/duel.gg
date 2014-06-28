package us.woop.pinger.service
import akka.actor.ActorDSL._
import akka.actor.{ActorRef, Kill, Props}
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingerController.{Unmonitor, Monitor}
import PingPongProcessor._
import us.woop.pinger.service.individual.ServerSupervisor

/**
 * This class creates a monitoring ecosystem that will
 * monitor and unmonitor different servers.
 *
 * It will launch child actors for new servers
 * which will contain a monitor and a rate controller
 *
 * It will route pinger responses into the event stream.
 */
object PingerController {
  case object Ready
  case class Monitor(server: Server)
  case class Unmonitor(server: Server)
}
class PingerController extends Act with ActWithStash {
  case class Pinger(actorRef: ActorRef)

  val servers = scala.collection.mutable.Map[Server, ActorRef]()

  whenStarting {
    self ! Pinger(context.actorOf(Props[PingPongProcessorActor], "pinger"))
    context.system.eventStream.subscribe(self, classOf[Ping])
  }

  become {
    case _: Monitor => stash()
    case _: Unmonitor => stash()
    case _: Ready => stash()
    case Pinger(pinger) =>
      become {
        case m: Monitor => stash()
        case m: Unmonitor => stash()

        case Ready(addr) =>
          unstashAll()
          context.parent ! PingerController.Ready
          become {
            case badHash: BadHash =>
              context.system.eventStream.publish(badHash)
            case receivedBytes: ReceivedBytes =>
              context.system.eventStream.publish(receivedBytes)
            case Monitor(server) =>
              val actorName = s"${server.ip.ip}:${server.port}"
              servers.getOrElseUpdate(
                key = server,
                op = context.actorOf(
                  name = actorName,
                  props = Props(classOf[ServerSupervisor], server)
                )
              )
            case Unmonitor(server) =>
              servers.remove(server).foreach(_ ! Kill)
            case p: Ping =>
              pinger ! p
          }
      }
  }
}
