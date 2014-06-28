package us.woop.pinger.data.actor

import us.woop.pinger.client.PingPongProcessor
import PingPongProcessor.Server

object ParsedSubscriber {
  case class Subscribe(server: Server)
  case class Unsubscribe(server: Server)
}
