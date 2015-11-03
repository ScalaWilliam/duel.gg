package modules

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject._

import akka.actor.{Cancellable, ActorSystem}
import akka.stream.scaladsl._
import modules.OgroDemoParser.Demo
import modules.sse.CancellableServerSentEventClient
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Future, ExecutionContext}

trait DemoCollection {

  def demoFetching: Source[DemosListing, Cancellable]
}

@Singleton
final class DemoCollectorEmpty @Inject()(implicit actorSystem: ActorSystem) extends DemoCollection {

  import concurrent.duration._

  override def demoFetching: Source[DemosListing, Cancellable] = Source.apply(0.seconds, 15.minutes, DemosListing.empty)
}

@Singleton
final class DemoCollectorLive @Inject()(ogroDemoParser: OgroDemoParser,
                                    applicationLifecycle: ApplicationLifecycle)
                                   (implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
extends DemoCollection {

  def getDemosF: Future[DemosListing] = Future.sequence(OgroDemoParser.servers.values.map { server =>
    ogroDemoParser.getDemosF(server).map { demos => server -> demos }
  }).map { demos => DemosListing(demos.toMap.mapValues(_.toSet)) }

  import concurrent.duration._

  override def demoFetching = Source.apply(0.seconds, 15.minutes, ()).mapAsyncUnordered(5) { _ => getDemosF }

}

case class DemosListing(demos: Map[String, Set[Demo]]) {
  def ++(demosListing: DemosListing): DemosListing = {
    DemosListing(
      demos = (demos.keySet ++ demosListing.demos.keySet).map { k =>
        k -> (demos.get(k).toSet.flatten ++ demosListing.demos.get(k).toSet.flatten)
      }.toMap
    )
  }

  def lookupFromGame(server: String, mode: String, map: String, atTime: ZonedDateTime): Option[String] = {
    PartialFunction.condOpt(server) {
      case OgroDemoParser.ServerName(sn) =>
        demos.get(sn)
    }.flatten.flatMap {
      _.collect { case demo
        if demo.map == map && ChronoUnit.MINUTES.between(demo.dateTime.toInstant, atTime.toInstant) <= 10 =>
        demo -> ChronoUnit.MINUTES.between(demo.dateTime.toInstant, atTime.toInstant)
      }.toList.sortBy(_._2).headOption.map(_._1)
    }.map(_.url)
  }
}

object DemosListing {
  def empty = DemosListing(demos = Map.empty)
}
