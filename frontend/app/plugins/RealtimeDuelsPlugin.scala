package plugins

import akka.actor.{Props, ActorRef, ActorLogging}
import akka.util.Timeout
import com.hazelcast.core.{EntryEvent, MapEvent, EntryListener}
import play.api.libs.concurrent.Akka
import play.api.{Play, Plugin, Application}
import plugins.DuelStoragePlugin.Duel
import plugins.RealtimeDuelsPlugin._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import akka.actor.ActorDSL._
import akka.pattern.pipe
import akka.pattern.ask
import concurrent.duration._
object RealtimeDuelsPlugin {
  def sampleLiveXml = <live-duel seconds-remaining="527" simple-id="2015-02-01T12:29:30Z::62.75.213.175:28785" duration="10" start-time="2015-02-01T12:29:30Z" map="ot" mode="instagib" server="62.75.213.175:28785">
    <played-at>9 10</played-at>
    <player name="vaQ'Frosty" partial-ip="68.150.207.x" frags="13" accuracy="39" weapon="rifle">
      <frags at="1">12</frags><frags at="2">13</frags>
    </player><player name="Elite" partial-ip="122.210.168.x" frags="14" accuracy="45" weapon="rifle">
    <frags at="1">13</frags><frags at="2">14</frags>
  </player>
  </live-duel>
  def sampleLiveXml2 = <live-duel seconds-remaining="527" simple-id="2015-02-01T12:29:30Z::62.75.213.175:22785" duration="10" start-time="2015-02-01T12:29:30Z" map="ot" mode="instagib" server="62.75.213.175:28785">
    <played-at>9 10</played-at>
    <player name="vaQ'Frostesh" partial-ip="68.150.207.x" frags="13" accuracy="39" weapon="rifle">
      <frags at="1">12</frags><frags at="2">13</frags>
    </player><player name="E-Lite" partial-ip="122.210.168.x" frags="14" accuracy="45" weapon="rifle">
    <frags at="1">13</frags><frags at="2">14</frags>
  </player>
  </live-duel>
  def plugin: RealtimeDuelsPlugin = Play.current.plugin[RealtimeDuelsPlugin]
    .getOrElse(throw new RuntimeException("RealtimeDuelsPlugin plugin not loaded"))
  case class ServerUpdated(serverId: String, xmlData: String)
  case class ServerEnriched(serverId: String, duel: RealtimeDuel)
  case class ServerRemoved(serverId: String)
  case class CurrentStatus(duels: Vector[RealtimeDuel]) {
    def toJson = s"""[${duels.map(_.jsonData).mkString(", ")}]"""
  }
  case object EverythingCleared
  case object GiveStatus
  case class RealtimeDuel(id: Int, dateTime: Long, users: Set[String], nicknames: Set[String], jsonData: String)
  object RealtimeDuel { def fromXml(elem: scala.xml.Elem) = RealtimeDuel((elem \@ "id").toInt, (elem \@ "at").toLong, (elem \@ "users").split(" ").toSet, (elem \@ "nicknames").split(" ").toSet, elem \@ "json") }
  object DuelUpdatesSenderActor {
    def props(out: ActorRef) = Props(new DuelUpdatesSenderActor(out))
  }
  import akka.actor.ActorDSL._
  class DuelUpdatesSenderActor(out: ActorRef) extends Act {
    whenStarting {
      context.system.eventStream.subscribe(self, classOf[CurrentStatus])
      RealtimeDuelsPlugin.plugin.giveStatus pipeTo self
    }
    become {
      case status: CurrentStatus => out ! status.toJson
    }
  }
}
class RealtimeDuelsPlugin(implicit app: Application) extends Plugin {
  implicit lazy val as = Akka.system
  def giveStatus: Future[CurrentStatus] = {
    implicit val timeout = Timeout(1.second)
    ask(good, GiveStatus).mapTo[CurrentStatus]
  }
  lazy val theMap = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("live-duel-updates")
  lazy val good = actor(new Act with ActorLogging {
    val servers = scala.collection.mutable.Map.empty[String, RealtimeDuel]
    case object Tick
    whenStarting {
//      context.system.scheduler.schedule(1.second, 5.seconds, self, ServerUpdated("wat?", RealtimeDuelsPlugin.sampleLiveXml.toString))
//      context.system.scheduler.schedule(2.second, 5.seconds, self, ServerUpdated("uw0t", RealtimeDuelsPlugin.sampleLiveXml2.toString))
      context.system.scheduler.schedule(0.seconds, 5.seconds, self, Tick)
      import collection.JavaConverters._
      theMap.asScala.foreach{case (k, v) => self ! ServerUpdated(k, v)}
    }
    become {
      case ServerUpdated(serverId, xmlData) =>
        async {
          val xmlElem = scala.xml.XML.loadString(xmlData)
          val enrichedData = await(DataSourcePlugin.plugin.enrichLiveDuel(xmlElem))
          ServerEnriched(serverId, enrichedData)
        } pipeTo self
      case akka.actor.Status.Failure(reason) =>
        log.error(reason, s"Failed to process live duel due to $reason")
      case ServerEnriched(serverId, duel) =>
        servers += serverId -> duel
      case ServerRemoved(serverId) =>
        servers.remove(serverId)
      case EverythingCleared =>
        servers.clear()
      case GiveStatus =>
        sender() ! CurrentStatus(servers.values.toVector)
      case Tick =>
        context.system.eventStream.publish(CurrentStatus(servers.values.toVector))
    }
  })
  lazy val listenerId = theMap.addEntryListener(new EntryListener[String, String] {
    override def entryAdded(event: EntryEvent[String, String]): Unit = {good ! ServerUpdated(event.getKey, event.getValue)}
    override def entryUpdated(event: EntryEvent[String, String]): Unit = {good ! ServerUpdated(event.getKey,event.getValue)}
    override def entryEvicted(event: EntryEvent[String, String]): Unit = {good!ServerRemoved(event.getKey)}
    override def mapEvicted(event: MapEvent): Unit = {good!EverythingCleared}
    override def entryRemoved(event: EntryEvent[String, String]): Unit = {good!ServerRemoved(event.getKey)}
    override def mapCleared(event: MapEvent): Unit = {good !EverythingCleared}
  }, true)

  override def onStart(): Unit = {
    good
    listenerId
//    try {
//      DataSourcePlugin.plugin.enrichLiveDuel(RealtimeDuelsPlugin.sampleLiveXml)
//      good
//      listenerId
//    } catch {
//      case e => println(e); throw e
//    }
  }

  override def onStop(): Unit = {
//    theMap.removeEntryListener(listenerId)
  }
}