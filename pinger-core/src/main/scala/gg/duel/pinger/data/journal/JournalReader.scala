package gg.duel.pinger.data.journal

import java.io.{File, FileInputStream}
import java.util.zip.{Deflater, DeflaterInputStream}

import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.journal.JournalReader.Game

object JournalReader {
  type Game = Either[SimpleCompletedDuel, SimpleCompletedCTF]
}

class JournalReader(file: File) {

  val fis = new FileInputStream(file)
  val is = new DeflaterInputStream(fis, new Deflater(Deflater.BEST_COMPRESSION))

  def getGames: Vector[Game] = {
    getSauerBytes
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
  }

  def close(): Unit = {
    is.close()
    fis.close()
  }


  def getSauerBytes: Iterator[SauerBytes] =
    Iterator.continually(
      elem = SauerBytesWriter.readSauerBytes(
        get = SauerBytesWriter.inputStreamNumBytes(is)
      )
    ).takeWhile(_.isDefined)
      .flatten
}