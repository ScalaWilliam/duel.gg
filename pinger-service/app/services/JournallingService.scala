package services

import java.io.{File, FileOutputStream, FileWriter}
import java.time.{LocalDateTime, ZonedDateTime}
import java.util.zip.{Deflater, DeflaterOutputStream}
import javax.inject._

import akka.actor.{Kill, ActorSystem}
import gg.duel.pinger.data.journal.{SauerBytesWriter, SauerBytesBinary, SauerBytes}
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JournallingService @Inject()(applicationLifecycle: ApplicationLifecycle)(implicit actorSystem: ActorSystem,
executionContext: ExecutionContext) {

  import akka.actor.ActorDSL._

  val targetFilename = s"${LocalDateTime.now().withNano(0).toString.replaceAllLiterally(":","")}.sblog"

  val targetFile = new File(targetFilename)

  Logger.info(s"Journalling to $targetFilename (${targetFile.getCanonicalFile})")

  val theFile = new FileOutputStream(targetFile)

  val compressedFile = new DeflaterOutputStream(theFile, new Deflater(Deflater.BEST_COMPRESSION), true)

  val push = SauerBytesWriter.createInjectedWriter{bytes =>
    compressedFile.write(bytes)
    compressedFile.flush()
    theFile.flush()
  }

  val myActor = actor(name = "journaller")(new Act {
    become {
      case sauerBytes: SauerBytes =>
        push(sauerBytes)
    }
  })

  actorSystem.eventStream.subscribe(myActor, classOf[SauerBytes])

  applicationLifecycle.addStopHook(() => Future {
    myActor ! Kill
    compressedFile.close()
    theFile.close()
  })

}