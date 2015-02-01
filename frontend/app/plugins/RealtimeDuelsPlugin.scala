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
  case class EnrichedDuels(duels: Vector[RealtimeDuel]) {
    def toJson = s"""[${duels.map(_.jsonData).mkString(", ")}]"""
  }
  object EnrichedDuels {
    def empty = EnrichedDuels(Vector.empty)
  }
  case object GiveStatus
  case class RealtimeDuel(id: Int, dateTime: Long, users: Set[String], nicknames: Set[String], jsonData: String)
  object RealtimeDuel { def fromXml(elem: scala.xml.Elem) = RealtimeDuel((elem \@ "id").toInt, (elem \@ "at").toLong, (elem \@ "users").split(" ").toSet, (elem \@ "nicknames").split(" ").toSet, elem \@ "json") }
  object DuelUpdatesSenderActor {
    def props(out: ActorRef) = Props(new DuelUpdatesSenderActor(out))
  }
  import akka.actor.ActorDSL._
  class DuelUpdatesSenderActor(out: ActorRef) extends Act {
    whenStarting {
      context.system.eventStream.subscribe(self, classOf[EnrichedDuels])
      RealtimeDuelsPlugin.plugin.giveStatus pipeTo self
    }
    become {
      case status: EnrichedDuels => out ! status.toJson
    }
  }
}
class RealtimeDuelsPlugin(implicit app: Application) extends Plugin {
  implicit lazy val as = Akka.system
  def giveStatus: Future[EnrichedDuels] = {
    implicit val timeout = Timeout(1.second)
    ask(good, GiveStatus).mapTo[EnrichedDuels]
  }
  lazy val theMap = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("live-duel-updates")
  lazy val good = actor(new Act with ActorLogging {
    var enrichedDuels = EnrichedDuels.empty
    case object Tick
    whenStarting {
      context.system.scheduler.schedule(0.seconds, 5.seconds, self, Tick)
    }
    become {
      case ed: EnrichedDuels =>
        enrichedDuels = ed
        context.system.eventStream.publish(ed)
      case akka.actor.Status.Failure(reason) =>
        log.error(reason, s"Failed to process live duel due to $reason")
      case GiveStatus =>
        sender() ! enrichedDuels
      case Tick =>
        import collection.JavaConverters._
        async {
          EnrichedDuels(await(DataSourcePlugin.plugin.enrichLiveDuels(theMap.asScala.valuesIterator.toVector.map(scala.xml.XML.loadString))))
        } pipeTo self
    }
  })

  override def onStart(): Unit = {
    good
  }

  override def onStop(): Unit = {
  }
}