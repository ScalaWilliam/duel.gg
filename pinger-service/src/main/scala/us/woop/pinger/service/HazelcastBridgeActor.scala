package us.woop.pinger.service
import akka.actor.ActorDSL._
import akka.actor.Props
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.{ItemEvent, ItemListener}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingerController.{Unmonitor, Monitor}
import us.woop.pinger.service.individual.ServerMonitor.ServerStateChanged
import us.woop.pinger.{MyId, SystemConfiguration}

import scala.util.Try
object HazelcastBridgeActor {
  def props = Props(classOf[HazelcastBridgeActor])
}
class HazelcastBridgeActor extends Act {
  val hazelcast = {
    val clientConfig = new ClientConfig()
    clientConfig.getGroupConfig.setName("dev").setPassword(SystemConfiguration.hazelcastGroupPassword)
    clientConfig.getNetworkConfig.addAddress("scala.contractors", "scalawilliam.com")
    HazelcastClient.newHazelcastClient(clientConfig)
  }
  val myParsedMessages = {
    val topicName = s"parsedMessages-${MyId.default}"
    hazelcast.getTopic[ParsedMessage](topicName)
  }

  val statusChanges = {
    val topicName = s"statusChanges-${MyId.default}"
    hazelcast.getTopic[ServerStateChanged](topicName)
  }

  val monitoredServersSet = {
    hazelcast.getSet[String]("servers")
  }

  var listenerId: String = _

  whenStarting {
    import collection.JavaConverters._
    monitoredServersSet.asScala.toVector.flatMap(x => Try(Server.apply(x)).toOption.toVector)
    val listenerI = new ItemListener[String] {
      def itemAdded(item: ItemEvent[String]): Unit = {
        self ! Monitor(Server(item.getItem))
      }

      def itemRemoved(item: ItemEvent[String]): Unit = {
        self ! Unmonitor(Server(item.getItem))
      }
    }
    listenerId = monitoredServersSet.addItemListener(listenerI, true)
  }

  become {
    case p: ParsedMessage => myParsedMessages.publish(p)
    case s: ServerStateChanged => statusChanges.publish(s)
  }

  whenStopping {
    // protect against potential memory leak
    monitoredServersSet.removeItemListener(listenerId)
  }

}
