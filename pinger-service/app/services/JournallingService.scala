package services

import java.io.{File, FileOutputStream, FileWriter}
import java.time.{LocalDateTime, ZonedDateTime}
import java.util.zip.{Deflater, DeflaterOutputStream}
import javax.inject._

import akka.actor.{Kill, ActorSystem}
import gg.duel.pinger.data.journal.{JournalWriter, SauerBytesWriter, SauerBytesBinary, SauerBytes}
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JournallingService @Inject()(applicationLifecycle: ApplicationLifecycle)(implicit actorSystem: ActorSystem,
executionContext: ExecutionContext) {

  import akka.actor.ActorDSL._

  val targetFilename = s"${LocalDateTime.now().withNano(0).toString.replaceAllLiterally(":","")}.sblog.gz"

  val targetFile = new File(s"../$targetFilename")

  Logger.info(s"Journalling to $targetFile (${targetFile.getCanonicalFile})")

  val jw = new JournalWriter(targetFile)

  val myActor = actor(name = "journaller")(new Act {
    become {
      case sauerBytes: SauerBytes =>
        jw.write(sauerBytes)
    }
  })

  actorSystem.eventStream.subscribe(myActor, classOf[SauerBytes])

  applicationLifecycle.addStopHook(() => Future {
    myActor ! Kill
    jw.close()
  })

}