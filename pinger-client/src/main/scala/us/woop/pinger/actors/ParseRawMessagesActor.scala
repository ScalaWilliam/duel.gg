package us.woop.pinger.actors

import akka.actor.ActorDSL._
import us.woop.pinger.data.actor.{ParsedProcessor, PingPongProcessor}
import PingPongProcessor.ReceivedBytes
import akka.actor.{Terminated, ActorRef}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.Extractor

class ParseRawMessagesActor extends Act {
  import ParsedProcessor._
  val firehose = collection.mutable.Set[ActorRef]()
  val extractor = Extractor.extract.lift
  become {
    case Terminated(client) =>
      context unwatch client
      firehose -= client
    case Subscribe =>
      firehose += sender()
    case Unsubscribe =>
      firehose -= sender()
    case ReceivedBytes(server, time, message) if firehose.nonEmpty =>
      for {
        results <- extractor apply message
        result <- results
        message = ParsedMessage(server, time, result)
        target <- firehose
      } target ! message
  }

}
