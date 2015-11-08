package services.demos


import akka.actor.Cancellable
import akka.stream.scaladsl._

trait DemoCollection {

  def demoFetching: Source[DemosListing, Cancellable]
}
