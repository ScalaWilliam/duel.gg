package services.demos


import javax.inject._

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import lib.OgroDemoParser
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

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
