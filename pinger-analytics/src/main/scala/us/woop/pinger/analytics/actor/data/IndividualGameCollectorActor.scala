package us.woop.pinger.analytics.actor.data

import us.woop.pinger.data.Stuff.Server


object IndividualGameCollectorActor {
  case class HaveGame(server: Server, data: String)
}
