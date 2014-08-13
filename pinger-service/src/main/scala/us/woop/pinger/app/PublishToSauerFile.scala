package us.woop.pinger.app

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorDSL._
import us.woop.pinger.data.persistence.{SauerReaderWriter, SauerWriter}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes

class PublishToSauerFile extends Act {

  var currentStore: SauerWriter = _

  case object Rotate

  def newUUID = java.util.UUID.randomUUID().toString

  def startWriting(): Unit = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm")
    val dateFormatted = dateFormat.format(new Date())
    val fn = new File(s"db-$dateFormatted-$newUUID.db")
    currentStore = SauerReaderWriter.writeToFile(fn)
    become(writing(currentStore))
  }

  whenStarting {
    startWriting()
    import context.dispatcher

import scala.concurrent.duration._
    context.system.scheduler.schedule(1.day, 1.day, self, Rotate)
  }

  def writing(to: SauerWriter): Receive = {
    case Rotate =>
      to.flush()
      to.close()
      startWriting()
    case m @ ReceivedBytes(server, time, message) =>
      to.write(m.toSauerBytes)
      to.flush()
  }

  whenStopping {
    if ( currentStore != null ) {
      currentStore.close()
    }
  }

}
