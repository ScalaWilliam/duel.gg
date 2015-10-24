package gg.duel.pinger.analytics.ctf

import gg.duel.pinger.analytics.ctf.CtfState.CtfAccumulation
import gg.duel.pinger.analytics.duel.GameHeader
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic.Accumulation._
import org.scalactic._


object CTFGame {

  def beginCTFParsing(parsedMessage: ParsedMessage): CtfState Or Every[ErrorMessage] = {
    parsedMessage match {
      case ParsedMessage(time, message: ConvertedServerInfoReply) =>
        beginCtfCSIR(time, message)
      case other =>
        Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName} = ${other.message}"))
    }
  }

  def beginCtfCSIR(startTime: Long, message: ConvertedServerInfoReply): CtfState Or Every[ErrorMessage] = {

    // todo wut, no mastermode? wtf!

    val clients =
      if (message.clients >= 4) Good(message.clients)
      else Bad(One(s"Expected 4 or more clients, got ${message.clients}"))

    val ctfModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
      case Some(modeName) if CtfState.ctfModeNames contains modeName => Good(modeName)
      case other => Bad(One(s"Mode $other (${message.gamemode}) not a ctf mode, expected one of ${CtfState.ctfModeNames}"))
    }

    val hasEnoughTime =
      if (message.remain > 540) Good(true)
      else Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)"))

    withGood(clients, ctfModeName, hasEnoughTime) { (_, modeName, _) =>
      val gameHeader = GameHeader(
        startTime = startTime,
        startMessage = message,
        mode = modeName,
        message = message.mapname
      )
      val ctfAccumulation = CtfAccumulation(List.empty, List.empty)
      TransitionalCtf(
        gameHeader = gameHeader,
        ctfAccumulation = ctfAccumulation,
        isRunning = !message.gamepaused,
        timeRemaining = message.remain
      )
    }
  }
}
