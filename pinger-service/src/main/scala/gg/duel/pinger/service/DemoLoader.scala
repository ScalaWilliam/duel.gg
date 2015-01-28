package gg.duel.pinger.service

import java.io.File
import java.net.URI
import java.nio.file.{StandardCopyOption, Files}

import akka.actor.Props
import gg.duel.pinger.service.DemoLoader.DemoDownloaded

import scala.concurrent.Future
import scala.util.{Failure, Success}

object DemoLoader {
  case class FoundGameId(gameId: String)
  def props(saveToDirectory: File, demoChecker: DemoChecker) = Props(new DemoLoader(saveToDirectory, demoChecker))
  case class DemoDownloaded(gameId: String, from: URI, destination: File)
}
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import gg.duel.pinger.app.Woot.MetaCompletedCtf
import gg.duel.pinger.app.Woot.MetaCompletedDuel
import gg.duel.pinger.service.DemoLoader.FoundGameId

class DemoLoader(saveToDirectory: File, demoChecker: DemoChecker) extends Act with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  become {
    case mcd: MetaCompletedDuel =>
      self ! FoundGameId(mcd.completedDuel.simpleId)
    case mcc: MetaCompletedCtf =>
      self ! FoundGameId(mcc.completedCtf.simpleId)
    case dd @ DemoDownloaded(gameId, from, destination) =>
      for {
        _ <- demoChecker.downloadedDemo(gameId, from.toString, destination.getAbsolutePath)
      } {
        context.parent ! dd
      }
    case FoundGameId(gameId) =>
      log.info(s"Found game Id: $gameId")
      for {
        _ <- demoChecker.checkDemo(gameId)
        demoLinkO <- demoChecker.getDemoLink(gameId)
      } {
        demoLinkO match {
          case Some(demoLink) =>
            val uri = new URI(demoLink)
            val cleanGameId = gameId.replaceAllLiterally(":", "_")
            val destination = new File(saveToDirectory, s"$cleanGameId.dmo").getCanonicalFile
            Future {
              Files.copy(uri.toURL.openStream(), destination.toPath, StandardCopyOption.REPLACE_EXISTING)
              DemoDownloaded(gameId, uri, destination)
            }.onComplete {
              case Success(demoDownloaded) => self ! demoDownloaded
              case Failure(reason) =>
                log.error(s"Failed to download demo for $gameId from $uri: {}", reason)
            }
          case None =>
            log.info(s"No demo could be found for game $gameId")
        }
      }
  }
}
