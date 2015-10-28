package services

import java.io.{File, FileInputStream}
import java.util.zip.{Deflater, DeflaterInputStream}
import javax.inject._

import akka.agent.Agent
import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.journal.{JournalReader, SauerBytesWriter}
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Promise, ExecutionContext}
import scala.util.control.NonFatal

@Singleton
class ReadJournalledService @Inject()(journallingService: JournallingService)
                                     (applicationLifecycle: ApplicationLifecycle)
                                     (implicit executionContext: ExecutionContext){

  import JournalReader._
  val journalFiles = new File(".").listFiles()
    .filter(_.getName.endsWith(".sblog.gz"))
    .filter(_.getName != journallingService.targetFilename)
    .toList
    .sorted

  val parsedGamesAgent = Agent(Vector.empty[Game])

  private val parsedGamesPromise = Promise[Vector[Game]]()

  val parsedGamesFuture = parsedGamesPromise.future

  val runProcess = new Runnable {
    override def run(): Unit = {
      Logger.info(s"Reading from the following journal files: ${journalFiles.mkString(", ")}")
      for { journalFile <- journalFiles } {
        Logger.info(s"Reading journal file $journalFile")
        val reader = new JournalReader(journalFile)
        val newGames =
          try reader.getGames
          catch {
            case NonFatal(e) =>
              Logger.error(s"Could not process games for $journalFile due to $e", e)
              Vector.empty
          }
          finally reader.close()
        parsedGamesAgent.send(_ ++ newGames)
        Logger.info(s"Finished reading journal file $journalFile, found ${newGames.size}")
      }
      Thread.sleep(100)
      Logger.info(s"Read ${parsedGamesAgent.get().size} games in total.")
      parsedGamesPromise.success(parsedGamesAgent.get())
    }
  }


  private val readJournalThread = new Thread(runProcess, "read-journal")

  readJournalThread.start()

}
