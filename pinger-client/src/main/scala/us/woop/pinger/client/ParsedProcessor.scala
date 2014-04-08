package us.woop.pinger.client
import akka.actor.ActorDSL._
import us.woop.pinger.client.data.{PingPongProcessor, ParsedProcessor}
import PingPongProcessor.{ReceivedMessage, Server}
import akka.actor.{Terminated, ActorRef}
import us.woop.pinger.client.data.ParsedProcessor


class ParsedProcessor extends Act {
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
    case ReceivedMessage(server, time, message) if firehose.nonEmpty =>
      for {
        results <- extractor apply message
        result <- results
        message = ParsedMessage(server, time, result)
        target <- firehose
      } target ! message
  }

}
