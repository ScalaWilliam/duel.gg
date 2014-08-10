package us.woop.pinger.service
import akka.actor.ActorDSL._
import akka.actor.{ActorRef, ActorLogging, Props}
import com.hazelcast.config.Config
import com.hazelcast.core._
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.Server
import us.woop.pinger.service.HazelcastBridgeActor.{ReceivedUnmonitorServer, ReceivedMonitorServer, HazelcastConnectionLost}
import us.woop.pinger.service.PingPongProcessor.{SerializableBytes, ReceivedBytes}
import us.woop.pinger.service.PingerController.{Monitor, Unmonitor}
import us.woop.pinger.service.individual.ServerMonitor.{ServerState, ServerStateChanged}
import scala.util.Try
import scala.util.control.NonFatal

object HazelcastBridgeActor {
  def props(hazelcastInstance: HazelcastInstance, parentActor: Option[ActorRef] = None) =
    Props(classOf[HazelcastBridgeActor], hazelcastInstance, parentActor)
  class HazelcastConnectionLost extends Throwable
  case class ReceivedMonitorServer(server: String)
  case class ReceivedUnmonitorServer(server: String)
}

class HazelcastBridgeActor(hazelcast: HazelcastInstance, parentActor: Option[ActorRef]) extends Act with ActorLogging {

  case object ClientShutdown

  val lifecycleListener = new LifecycleListener {
    override def stateChanged(event: LifecycleEvent): Unit = {
      if ( event.getState eq LifecycleEvent.LifecycleState.SHUTDOWN ) {
        implicit val ec = context.dispatcher
        self ! ClientShutdown
      }
    }
  }

  val lifecycleListenerId = hazelcast.getLifecycleService.addLifecycleListener(lifecycleListener)

  val serverListener = new ItemListener[String] {

    def itemAdded(item: ItemEvent[String]): Unit = {
      self ! ReceivedMonitorServer(item.getItem)
      val server = Server(item.getItem)
      self ! Monitor(server)
      log.info("Added server {}", item)
    }

    def itemRemoved(item: ItemEvent[String]): Unit = {
      self ! ReceivedUnmonitorServer(item.getItem)
      val server = Server(item.getItem)
      self ! Unmonitor(server)
      log.info("Removed server {}", item)
    }

  }

  val monitoredServersSet = {
    hazelcast.getSet[String]("servers")
  }

  val serverListenerId = monitoredServersSet.addItemListener(serverListener, true)

  whenStopping {
    hazelcast.getLifecycleService.removeLifecycleListener(lifecycleListenerId)
    monitoredServersSet.removeItemListener(serverListenerId)
    hazelcast.shutdown()
  }

  whenStarting {
    import scala.collection.JavaConverters._
    val serversSet = monitoredServersSet.asScala.toVector.flatMap(x => Try(Server.apply(x)).toOption.toVector)
    for { server <- serversSet } {
      log.debug("Server found: {}", server)
      self ! Monitor(server)
    }
  }

  become {
    case ClientShutdown =>
      throw new HazelcastConnectionLost
    case m: Monitor =>
      parentActor.getOrElse(context.parent) ! m
    case ReceivedMonitorServer(address) =>
      try {
        val server = Server(address)
        self ! Monitor(server)
        log.info("Monitoring server {}", server)
      } catch {
        case NonFatal(_) =>
          log.info("Failed to add given server {}", address)
      }
    case ReceivedUnmonitorServer(address) =>
      for {
        server <- Try(Server(address)).toOption
      } {
        self ! Monitor(server)
        log.info("Unmonitoring server {}", server)
      }
    case u: Unmonitor =>
      parentActor.getOrElse(context.parent) ! u
    case p: ReceivedBytes=> receivedBytes.publish(p.toSerializable)
    case p: ParsedMessage => parsedMessages.publish(p)
    case s: ServerStateChanged =>
      stateChanges.publish(s)
      statusMap.put(s.server, s.serverState)
  }

  val statusMap = hazelcast.getMap[Server, ServerState]("server-states")
  val stateChanges = hazelcast.getTopic[ServerStateChanged]("server-states-changes")
  val parsedMessages = hazelcast.getTopic[ParsedMessage]("parsed-messages")
  val receivedBytes = hazelcast.getTopic[SerializableBytes]("received-bytes")

}
