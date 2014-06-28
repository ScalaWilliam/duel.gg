package us.woop.pinger.analytics.actor.data

import us.woop.pinger.client.PingPongProcessor
import PingPongProcessor.Server

object IndividualGameCollectorActor {
  case class HaveGame(server: Server, data: String)
}
