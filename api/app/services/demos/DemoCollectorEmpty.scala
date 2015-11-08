package services.demos


import javax.inject._

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl._

@Singleton
final class DemoCollectorEmpty @Inject()(implicit actorSystem: ActorSystem) extends DemoCollection {

  import concurrent.duration._

  override def demoFetching: Source[DemosListing, Cancellable] = Source.apply(0.seconds, 15.minutes, DemosListing.empty)
}
