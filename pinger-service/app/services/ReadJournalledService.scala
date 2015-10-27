package services

import java.io.{File, FileInputStream}
import javax.inject._

import akka.agent.Agent
import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.journal.SauerBytesWriter
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext

@Singleton
class ReadJournalledService @Inject()(journallingService: JournallingService)
                                     (applicationLifecycle: ApplicationLifecycle)
                                     (implicit executionContext: ExecutionContext){

  type Game = Either[SimpleCompletedDuel, SimpleCompletedCTF]

  val journalFiles = new File(".").listFiles()
    .filter(_.getName != journallingService.targetFilename)
    .filter(_.getName.endsWith(".sblog"))
    .toList
    .sorted

  def parseFile(file: File): Vector[Game] = {
    val is = new FileInputStream(file)
    try {
      Iterator.continually(
        elem = SauerBytesWriter.readSauerBytes(
          get = SauerBytesWriter.inputStreamNumBytes(is)
        )
      ).takeWhile(_.isDefined)
        .flatten
        .foldLeft((SIteratorState.empty, Vector.empty[Game])) {
          case ((state, games), sauerBytes) =>
            state.next.apply(sauerBytes) match {
              case nextState@SFoundGame(_, CompletedGame(completedGame, _)) =>
                (nextState, games :+ completedGame)
              case nextState =>
                (nextState, games)
            }
        } match {
        case (finalState, games) => games
      }
    } finally is.close()
  }

  val parsedGamesAgent = Agent(Vector.empty[Game])

  val runProcess = new Runnable {
    override def run(): Unit = {
      Logger.info(s"Reading from the following journal files: ${journalFiles.mkString(", ")}")
      for { journalFile <- journalFiles } {
        Logger.info(s"Reading journal file $journalFile")
        val newGames = parseFile(journalFile)
        parsedGamesAgent.send(_ ++ newGames)
        Logger.info(s"Finished reading journal file $journalFile, found ${newGames.size}")
      }
      Thread.sleep(100)
      Logger.info(s"Read ${parsedGamesAgent.get().size} games in total.")
    }
  }

  val readJournalThread = new Thread(runProcess, "read-journal")

  readJournalThread.start()

}
