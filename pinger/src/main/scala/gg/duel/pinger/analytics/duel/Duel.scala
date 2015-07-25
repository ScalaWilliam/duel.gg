package gg.duel.pinger.analytics.duel

import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic.Accumulation._
import org.scalactic._

object Duel {

  def beginDuelParsedMessage(parsedMessage: ParsedMessage): BetterDuelState Or Every[ErrorMessage] = {
    parsedMessage match {
      case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
        beginDuelCSIR(s, time, message)
      case other =>
        Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName} = ${other.message}"))
    }
  }

  def beginDuelCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): BetterDuelState Or Every[ErrorMessage] = {

    val clients =
      if (message.clients >= 2) Good(message.clients)
      else Bad(One(s"Expected 2 or more clients, got ${message.clients}"))

    val duelModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
      case Some(modeName) if BetterDuelState.duelModeNames contains modeName  => Good(modeName)
      case other => Bad(One(s"Mode $other (${message.gamemode}) not a duel mode, expected one of ${BetterDuelState.duelModeNames}"))
    }

    val hasEnoughTime =
      if ( message.remain > 540 ) Good(true)
      else Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)"))

    withGood(clients, duelModeName, hasEnoughTime) { (_, modeName, _) =>
      val gameHeader = GameHeader(startTime, message, s"${server.ip.ip}:${server.port}", modeName, message.mapname)
      val duelAccumulation = DuelAccumulation(List.empty)
      TransitionalBetterDuel(
        gameHeader = gameHeader,
        duelAccumulation = duelAccumulation,
        isRunning = !message.gamepaused,
        timeRemaining = SecondsRemaining(message.remain)
      )
    }
  }
}