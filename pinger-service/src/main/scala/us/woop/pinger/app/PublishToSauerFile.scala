package us.woop.pinger.app

import java.io.{FileWriter, FileOutputStream, File}
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import us.woop.pinger.data.journal.{SauerBytes, SauerBytesWriter, MetaData}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes

object PublishToSauerFile {

}
class PublishToSauerFile extends Act with ActorLogging {

  var currentStream: FileOutputStream = _
  var currentMetadata: MetaData = _
  var outputLog: File = _
  var outputJson: File = _

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
    import sys.process._
    Seq("bzip2", "-k", from.getCanonicalPath).run(ProcessLogger(log.debug, log.debug))
  }

  def startWriting(): Unit = {
    val metaData = newMetaData
    outputJson = new File(s"${metaData.id}.json")
    val os = new FileWriter(outputJson)
    os.write(metaData.toJson)
    os.flush()
    os.close()

    outputLog = new File(s"${metaData.id}.log")
    currentStream = new FileOutputStream(outputLog)
    become(writing(metaData, SauerBytesWriter.createInjectedWriter(b => {
      currentStream.write(b)
      currentStream.flush()
    })))
  }

  whenStarting {
    startWriting()
    import context.dispatcher
    import scala.concurrent.duration._
    context.system.scheduler.schedule(1.day, 1.day, self, Rotate)
  }

  def writing(metadata: MetaData, write: SauerBytes => Unit): Receive = {
    case Rotate =>
      stopWriting()
      startWriting()
    case m @ ReceivedBytes(server, time, message) =>
      write(m.toSauerBytes)
  }

  whenStopping {
    stopWriting()
  }

}
