package gg.duel.pinger.data.journal

import java.io.{BufferedInputStream, DataInputStream, File}
import java.net.URL
import java.util.zip.GZIPInputStream

import gg.duel.pinger.analytics.SIteratorState
import gg.duel.pinger.analytics.SIteratorState.{CompletedGame, SFoundGame}
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.journal.JournalReader.Game
import gg.duel.pinger.data.{SauerBytes, Server}

object JournalReader {
  type Game = SimpleCompletedDuel
}

class FilterJournalReader(url: URL, servers: Set[Server]) extends JournalReader(url) {
  def this(file: File, servers: Set[Server]) = this(file.toURI.toURL, servers)

  override def getSauerBytes = super.getSauerBytes.filter(sb => servers.contains(sb.server))
}

class JournalReader(url: URL) {
  def this(file: File) = this(file.toURI.toURL)

  val fis = url.openStream()
  val gis = if (url.toString.endsWith(".gz"))
    new GZIPInputStream(fis, 131072)
  else fis
  val is = new BufferedInputStream(gis, if (gis.isInstanceOf[GZIPInputStream]) 131072 else 32768)

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

  def getGamesIterator: Iterator[Game] = {
    getSauerBytes
      .scanLeft(SIteratorState.empty) { case (state, sauerBytes) =>
        state.next.apply(sauerBytes)
      }.collect { case SFoundGame(_, CompletedGame(completedGame, _)) => completedGame }
      .scanLeft(Option.empty[SimpleCompletedDuel]) { case (Some(scd), scd2) if scd == scd2 => None
      case (_, scd) => Some(scd)
      }
      .flatten
  }

  def close(): Unit = {
    is.close()
    gis.close()
    fis.close()
  }

  def getSauerBytes: Iterator[SauerBytes] = {
    new EfficientSauerByteReader(new DataInputStream(is)).toIterator
  }

}
