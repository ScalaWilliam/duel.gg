package gg.duel.pinger

import java.io.FileWriter
import java.nio.file.{Path, Paths}

import gg.duel.pinger.analytics.duel.BetterDuelState
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}

/**
  * Created by me on 10/07/2016.
  */
/**
  * Had an idea to optimise reading by excluding particular servers that are never played on.
  */
object KeepRecord {
  def isDuel(mode: Int): Boolean = {
    ModesList.modes.get(mode).map(_.name).exists(s => BetterDuelState.duelModeNames.contains(s))
  }

  def matching(g: ConvertedServerInfoReply): Boolean =
    g.clients >= 2 && isDuel(g.gamemode) && g.remain <= 1

  def empty: KeepRecord = KeepRecord(servers = Set.empty)

  def readFromFile(path: Path): KeepRecord = {
    val src = scala.io.Source.fromURL(path.toUri.toURL)
    try KeepRecord(src.getLines().map(Server.fromPrinter(_).get).toSet)
    finally src.close()
  }

  def pathFor(path: Path): Path = Paths.get(path + ".keep-servers.txt")
}

case class KeepRecord(servers: Set[Server]) {
  def include(parsedMessage: ParsedMessage): KeepRecord = {
    parsedMessage match {
      case ParsedMessage(server, _, g: ConvertedServerInfoReply) if KeepRecord.matching(g) =>
        copy(servers = servers + server)
      case _ => this
    }
  }

  def saveTo(path: Path): Unit = {
    val fw = new FileWriter(path.toFile)
    try fw.write(servers.mkString("\n"))
    finally fw.close()
  }
}
