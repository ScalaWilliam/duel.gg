package us.woop.pinger.service.journal

import java.io.{File, FileOutputStream, FileWriter}

import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import us.woop.pinger.data.journal.{IterationMetaData, SauerBytes, SauerBytesWriter}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.journal.JournalSauerBytes.{Rotate, WritingStopped}

object JournalSauerBytes {
  case class Rotate(metaData: IterationMetaData)
  case class WritingStopped(metaData: IterationMetaData)
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
