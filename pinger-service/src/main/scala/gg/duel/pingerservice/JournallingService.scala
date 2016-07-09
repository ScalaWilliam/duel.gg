package gg.duel.pingerservice

import java.io.File
import java.time.LocalDateTime
import javax.inject._

import akka.actor.{ActorSystem, Kill}
import gg.duel.pinger.data.SauerBytes
import gg.duel.pinger.data.journal.JournalWriter
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

class JournallingService (implicit actorSystem: ActorSystem,
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

  def stop(): Unit = {
    myActor ! Kill
    jw.close()
  }

}
