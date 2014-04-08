package us.woop.pinger.client.data

import PingPongProcessor.Server

object ParsedSubscriber {
  case class Subscribe(server: Server)
  case class Unsubscribe(server: Server)
}
