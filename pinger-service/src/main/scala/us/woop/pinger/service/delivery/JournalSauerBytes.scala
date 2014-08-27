package us.woop.pinger.service.delivery

import java.io.{File, FileOutputStream}

import akka.actor.ActorDSL._
import akka.actor.{Props, ActorLogging}
import us.woop.pinger.data.journal.{IterationMetaData, SauerBytesWriter}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.delivery.JournalSauerBytes.{Rotate, WritingStopped}

object JournalSauerBytes {
  case class Rotate(metaData: IterationMetaData)
  case class WritingStopped(metaData: IterationMetaData)
  def props(imd: IterationMetaData) = Props(classOf[JournalSauerBytes], imd)
}

class JournalSauerBytes(initialMetaData: IterationMetaData) extends Act with ActorLogging {

  var currentStream: FileOutputStream = _
  var outputLog: File = _
  var currentMetadata: IterationMetaData = initialMetaData

  def stopWriting(): Unit = {
    if ( currentStream != null ) {
      currentStream.flush()
      currentStream.close()
    }
    compress(outputLog)
    context.parent ! WritingStopped(currentMetadata)
  }

  def compress(from: File): Unit = {
    import scala.sys.process._
    Seq("bzip2", "-k", from.getCanonicalPath).run(ProcessLogger(log.debug, log.debug))
  }
  
  val writeToJournal = SauerBytesWriter.createInjectedWriter(b => {
    currentStream.write(b)
    currentStream.flush()
  })

  def startWriting(metaData: IterationMetaData): Unit = {
    outputLog = new File(s"${metaData.id}.log")
    currentStream = new FileOutputStream(outputLog)
    become(writingState(metaData))
    context.parent ! metaData
  }

  def writingState(metadata: IterationMetaData): Receive = {
    case m @ ReceivedBytes(server, time, message) =>
      writeToJournal(m.toSauerBytes)
    case Rotate(newMetaData) =>
      stopWriting()
      startWriting(newMetaData)
  }

  whenStarting {
    startWriting(initialMetaData)
  }

  whenStopping {
    stopWriting()
  }

}
