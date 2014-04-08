package us.woop.pinger.client.data

import PingPongProcessor.Server

object GlobalPingerClient {
  case class Monitor(server: Server)
  case class Unmonitor(server: Server)
  case object Listen
  case object Unlisten
}
