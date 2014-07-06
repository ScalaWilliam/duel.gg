package us.woop.pinger.service
import akka.actor.ActorDSL._
import akka.actor.{ActorRef, PoisonPill, Props}
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingPongProcessor._
import us.woop.pinger.service.PingerController.{Monitor, Unmonitor}
import us.woop.pinger.service.RawToExtracted.ExtractedMessage
import us.woop.pinger.service.individual.ServerMonitor.ServerStateChanged
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

  case class Dependencies(pinger: ActorRef, parser: ActorRef)

  val servers = scala.collection.mutable.Map[Server, ActorRef]()

  whenStarting {
    self ! Dependencies(
      pinger = context.actorOf(Props[PingPongProcessorActor], "pinger"),
      parser = context.actorOf(Props[RawToExtracted], "parser")
    )
  }


  become {
    case Dependencies(pinger, parser) =>
      become {
        case Ready(addr) =>
          unstashAll()
          context.parent ! PingerController.Ready
          become(ready(pinger, parser))
        case _ => stash()
      }
    case _ => stash()
  }

  def ready(pinger: ActorRef, parser: ActorRef): Receive = {
    case badHash: BadHash if servers contains badHash.server =>
      servers(badHash.server) ! badHash
      context.parent ! badHash
    case receivedBytes: ReceivedBytes if servers contains receivedBytes.server =>
      parser ! receivedBytes
      context.parent ! receivedBytes
    case serverStateChange: ServerStateChanged =>
      context.parent ! serverStateChange
    case e: ExtractedMessage[_] if servers contains e.server =>
      servers(e.server) ! e
      context.parent ! e
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
      servers.remove(server).foreach(_ ! PoisonPill)
    case p: Ping =>
      pinger ! p
  }


}
