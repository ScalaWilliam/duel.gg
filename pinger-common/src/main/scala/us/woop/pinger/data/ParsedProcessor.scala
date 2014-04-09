package us.woop.pinger.data

import PingPongProcessor.Server

/** Parse stuff into meaningful stuff and send out to all subscribers **/
object ParsedProcessor {
  case object Subscribe
  case object Unsubscribe
  case class ParsedMessage(server: Server, time: Long, message: Any)
}
