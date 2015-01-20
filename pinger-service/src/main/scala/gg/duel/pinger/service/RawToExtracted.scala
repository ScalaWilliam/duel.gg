package gg.duel.pinger.service

import akka.actor.ActorDSL._
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{Extractor, Server}
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes
import gg.duel.pinger.service.RawToExtracted.ExtractedMessage

object RawToExtracted {
  case class ExtractedMessage[T](server: Server, time: Long, message: T)
}
class RawToExtracted extends Act {
  become {
    case ReceivedBytes(server, time, message) =>
      for {
        obj <- Extractor.extract.lift.apply(message)
        item <- obj
        parsedMessage = ParsedMessage(Server(server.ip.ip, server.port), time, item)
        extractedMessage = ExtractedMessage(server, time, item)
      } {
        context.parent ! extractedMessage
        context.parent ! parsedMessage
      }
  }
}
