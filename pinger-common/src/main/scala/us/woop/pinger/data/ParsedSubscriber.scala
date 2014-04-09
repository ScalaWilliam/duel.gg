package us.woop.pinger.data

import PingPongProcessor.Server

object ParsedSubscriber {
  case class Subscribe(server: Server)
  case class Unsubscribe(server: Server)
}
