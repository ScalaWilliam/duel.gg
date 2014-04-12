package us.woop.pinger.data.actor

import PingPongProcessor.Server

object ParsedSubscriber {
  case class Subscribe(server: Server)
  case class Unsubscribe(server: Server)
}
