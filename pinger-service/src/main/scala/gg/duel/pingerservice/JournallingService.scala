package gg.duel.pingerservice

import java.io.File
import java.time.LocalDateTime

import akka.actor.{ActorSystem, Kill}
import gg.duel.pinger.data.SauerBytes
import gg.duel.pinger.data.journal.JournalWriter
import play.api.Logger

import scala.concurrent.ExecutionContext

class JournallingService(implicit actorSystem: ActorSystem,
                         executionContext: ExecutionContext) {

  import akka.actor.ActorDSL._

  var currentBucket: AtDate = AtDate.current()

  var jw = currentBucket.writeNow().newJournalWriter

  val myActor = actor(name = "journaller")(new Act {
    become {
      case sauerBytes: SauerBytes =>
        if (currentBucket != AtDate.current()) {
          jw.close()
          currentBucket = AtDate.current()
          jw = currentBucket.writeNow().newJournalWriter
        }
        jw.write(sauerBytes)
    }
  })

  actorSystem.eventStream.subscribe(myActor, classOf[SauerBytes])

  def stop(): Unit = {
    myActor ! Kill
    jw.close()
  }

}

case class AtDate(bucketTime: LocalDateTime) {

  case class Write(localDateTime: LocalDateTime) {

    def targetFilename =
      s"${localDateTime.withNano(0).toString.replaceAllLiterally(":", "")}.sblog.gz"

    def targetFile = new File(s"../${targetFilename}")

    def newJournalWriter = {
      Logger.info(s"Journalling to $targetFile (${targetFile.getCanonicalFile})")
      new JournalWriter(targetFile)
    }

  }

  def writeNow(): Write = Write(LocalDateTime.now())

}

object AtDate {
  def current(): AtDate = AtDate(LocalDateTime.now().minusHours(HOUR_SHIFT).withHour(0).withMinute(0).withSecond(0).withNano(0))

  // This is the hour of the morning where we decide that we should rotate the log as there's the least activity around this time.
  val HOUR_SHIFT = 6
}
