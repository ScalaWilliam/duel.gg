package gg.duel.pinger.service.analytics

import akka.actor.{Props, ActorLogging}
import akka.actor.ActorDSL._
import gg.duel.pinger.data.journal.{SauerBytes, IterationMetaData}
import gg.duel.pinger.service.analytics.JournalBytes.{WritingStopped, Writer}

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
