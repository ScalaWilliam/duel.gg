package us.woop.pinger.service.journal

import java.io.{File, FileOutputStream, FileWriter}

import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import us.woop.pinger.data.journal.{MetaData, SauerBytes, SauerBytesWriter}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.journal.JournalSauerBytes.Finished

object JournalSauerBytes {
  case class Finished(metaData: MetaData)
}
class JournalSauerBytes extends Act with ActorLogging {

  var currentStream: FileOutputStream = _
  var currentMetadata: MetaData = _
  var outputLog: File = _
  var outputJson: File = _
  var newCount: Int = _
  val countLimit = 5000000

  case object Rotate

  def newMetaData = MetaData.build

  def stopWriting(): Unit = {
    if ( currentStream != null ) {
      currentStream.flush()
      currentStream.close()
    }
    compress(outputLog)
  }

  def compress(from: File): Unit = {
    import scala.sys.process._
    Seq("bzip2", "-k", from.getCanonicalPath).run(ProcessLogger(log.debug, log.debug))
  }

  def startWriting(): Unit = {
    val metaData = newMetaData
    outputJson = new File(s"${metaData.id}.json")
    val os = new FileWriter(outputJson)
    os.write(metaData.toJson)
    os.flush()
    os.close()
    newCount = 0
    outputLog = new File(s"${metaData.id}.log")
    currentStream = new FileOutputStream(outputLog)
    become(writing(metaData, SauerBytesWriter.createInjectedWriter(b => {
      currentStream.write(b)
      currentStream.flush()
    })))
    context.parent ! metaData
  }

  whenStarting {
    startWriting()
  }

  def writing(metadata: MetaData, write: SauerBytes => Unit): Receive = {
    case Rotate =>
      context.parent ! Finished(metadata)
      stopWriting()
      startWriting()
    case m @ ReceivedBytes(server, time, message) =>
      newCount = newCount + 1
      write(m.toSauerBytes)
      if ( newCount == countLimit ) {
        self ! Rotate
      }
  }

  whenStopping {
    stopWriting()
  }

}
