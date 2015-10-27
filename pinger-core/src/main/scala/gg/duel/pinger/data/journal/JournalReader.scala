package gg.duel.pinger.data.journal

import java.io.{BufferedInputStream, File, FileInputStream}
import java.util.zip.{GZIPInputStream, InflaterInputStream, Deflater, DeflaterInputStream}

import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.journal.JournalReader.Game

object JournalReader {
  type Game = Either[SimpleCompletedDuel, SimpleCompletedCTF]
}

class JournalReader(file: File) {

  val fis = new FileInputStream(file)
  val gis = new GZIPInputStream(fis)
  val is = new BufferedInputStream(gis)

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
    gis.close()
    fis.close()
  }

  def getSauerBytes: Iterator[SauerBytes] = {
    new SauerByteInputStreamReader(is).toIterator
  }
}