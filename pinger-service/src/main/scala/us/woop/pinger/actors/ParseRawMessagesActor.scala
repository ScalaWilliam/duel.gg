package us.woop.pinger.actors

import akka.actor.ActorDSL._
import us.woop.pinger.data.actor.{ParsedProcessor, PingPongProcessor}
import PingPongProcessor.ReceivedBytes
import akka.actor.{ActorLogging, Terminated, ActorRef}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.Extractor

class ParseRawMessagesActor extends Act with ActorLogging {
  import ParsedProcessor._
  val firehose = collection.mutable.Set[ActorRef]()
  val extractor = Extractor.extract.lift

  whenStarting {
    log.info("Starting message parser actor...")
  }

  become {
    case ReceivedBytes(server, time, message) if firehose.nonEmpty =>
      for {
        results <- extractor apply message
        result <- results
        message = ParsedMessage(server, time, result)
        target <- firehose
      } target ! message
    case Terminated(client) =>
      context unwatch client
      firehose -= client
      log.info("Parser firehose subscriber died. Removing {}", sender())
    case Subscribe if !(firehose contains sender()) =>
      firehose += sender()
      log.info("Parser firehose subscription added: {}", sender())
    case Unsubscribe if firehose contains sender() =>
      firehose -= sender()
      log.info("Parser firehose subscription removed: {}", sender())
  }

}
