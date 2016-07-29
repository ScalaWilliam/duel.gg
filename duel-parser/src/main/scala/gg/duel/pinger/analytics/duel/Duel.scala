package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.DuelParseError._
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic._

object Duel {

  def beginDuelParsedMessage(parsedMessage: ParsedMessage): BetterDuelState Or DuelParseError = {
    parsedMessage match {
      case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
        beginDuelCSIR(s, time, message)
      case other =>
        Bad(InputNotConvertedServerInfoReply)
    }
  }

  def beginDuelCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): BetterDuelState Or DuelParseError = {

    val clients: Int Or DuelParseError =
      if (message.clients >= 2) Good(message.clients)
      else Bad(ExpectedMoreThan2Clients(message.clients))

    val duelModeName: String Or DuelParseError = ModesList.modes.get(message.gamemode).map(_.name) match {
      case Some(modeName) if BetterDuelState.duelModeNames contains modeName => Good(modeName)
      case other => Bad(NonDuelMode)
    }

    val hasEnoughTime: Boolean Or DuelParseError =
      if (message.remain > EXPECTED_TIME_SECONDS) Good(true)
      else Bad(RemainingTimeExpected(EXPECTED_TIME_SECONDS, message.remain))

    (clients, duelModeName, hasEnoughTime) match {
      case (Good(_), Good(modeName), Good(_)) =>
        val gameHeader = GameHeader(startTime, message, s"${server.ip.stringIp}:${server.port}", modeName, message.mapname)
        val duelAccumulation = DuelAccumulation.empty
        Good(TransitionalBetterDuel(
          gameHeader = gameHeader,
          duelAccumulation = duelAccumulation,
          isRunning = !message.gamepaused,
          timeRemaining = SecondsRemaining(message.remain)
        ))
      case (a, b, c) =>
        Bad(Multiple.dpe(Iterator(a, b, c).flatMap(_.toEither.left.toSeq).toSeq: _*))
    }
  }

  val EXPECTED_TIME_SECONDS = 540

}




