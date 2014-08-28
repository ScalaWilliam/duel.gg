package us.woop.pinger.service.analytics

import akka.actor.{Props, ActorLogging}
import akka.actor.ActorDSL._
import us.woop.pinger.data.journal.{SauerBytes, IterationMetaData}
import us.woop.pinger.service.analytics.JournalBytes.{WritingStopped, Writer}

object JournalBytes {
  case class WritingStopped(metaData: IterationMetaData)
  case class Writer(write: SauerBytes => Unit, stop: () => Unit)
  def props(imd: IterationMetaData, writer: Writer) = Props(classOf[JournalBytes], imd, writer)
}

class JournalBytes(metaData: IterationMetaData, writer: Writer) extends Act with ActorLogging {

  become {
    case b: SauerBytes =>
      writer.write(b)
  }

  whenStopping {
    writer.stop()
    context.parent ! WritingStopped(metaData)
  }

}
