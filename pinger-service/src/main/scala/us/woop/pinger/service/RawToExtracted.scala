package us.woop.pinger.service
import akka.actor.ActorDSL._
import us.woop.pinger.Extractor
import us.woop.pinger.data.Stuff.Server
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.RawToExtracted.ExtractedMessage

object RawToExtracted {
  case class ExtractedMessage[T](server: Server, time: Long, message: T)
}
class RawToExtracted extends Act {
  become {
    case ReceivedBytes(server, time, message) =>
      for {
        obj <- Extractor.extract.lift.apply(message)
        item <- obj
        extractedMessage = ExtractedMessage(server, time, item)
      } context.parent ! extractedMessage
  }
}
