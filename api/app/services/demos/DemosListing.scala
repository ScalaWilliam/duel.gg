package services.demos

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import lib.OgroDemoParser
import lib.OgroDemoParser.Demo

object DemosListing {
  def empty = DemosListing(demos = Map.empty)
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

