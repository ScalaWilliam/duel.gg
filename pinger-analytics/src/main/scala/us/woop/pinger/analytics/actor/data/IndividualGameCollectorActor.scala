package us.woop.pinger.analytics.actor.data


object IndividualGameCollectorActor {
  case class HaveGame(server: Server, data: String)
}
