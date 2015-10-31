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

  def readJournal[T](journalFile: File)(into: Game => T): Vector[T] = {
    Logger.info(s"Reading journal file $journalFile")
    val reader = new JournalReader(journalFile)
    val newGames =
      try reader.getGamesIterator.map(into).toVector
      catch {
        case NonFatal(e) =>
          Logger.error(s"Could not process games for $journalFile due to $e", e)
          Vector.empty
      }
      finally reader.close()
    Logger.info(s"Finished reading journal file $journalFile, found ${newGames.size} games.")
    newGames
  }

  val runProcess = new Runnable {
    override def run(): Unit = {
      Logger.info(s"Reading from the following journal files: ${journalFiles.mkString(", ")}")
      val foundGames = journalFiles.par.flatMap {
        f => readJournal(f) {
          g => {
            parsedGamesAgent.send(_ :+ g)
            g.fold(_.startTimeText, _.startTimeText)
          }
        }
      }.toVector
      println(s"Found games: $foundGames")
      Thread.sleep(100)
      Logger.info(s"Read ${parsedGamesAgent.get().size} games in total.")
      parsedGamesPromise.success(parsedGamesAgent.get())
    }
  }

  private val readJournalThread = new Thread(runProcess, "read-journal")

  readJournalThread.start()

}
