package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.DuelParseError.{ExpectedMoreThan2Clients, InputNotConvertedServerInfoReply, NonDuelMode, RemainingTimeExpected}
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic.Accumulation._
import org.scalactic._

import scala.collection.immutable.Queue

object Duel {

  def beginDuelParsedMessage(parsedMessage: ParsedMessage): BetterDuelState Or Every[DuelParseError] = {
    parsedMessage match {
      case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
        beginDuelCSIR(s, time, message)
      case other =>
        Bad(One(InputNotConvertedServerInfoReply))
    }
  }

  def beginDuelCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): BetterDuelState Or Every[DuelParseError] = {

    val clients =
      if (message.clients >= 2) Good(message.clients)
      else Bad(One(ExpectedMoreThan2Clients(message.clients)))

    val duelModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
      case Some(modeName) if BetterDuelState.duelModeNames contains modeName  => Good(modeName)
      case other => Bad(One(NonDuelMode))
    }

    val hasEnoughTime =
      if ( message.remain > EXPECTED_TIME_SECONDS ) Good(true)
      else Bad(One(RemainingTimeExpected(EXPECTED_TIME_SECONDS, message.remain)))

    withGood(clients, duelModeName, hasEnoughTime) { (_, modeName, _) =>
      val gameHeader = GameHeader(startTime, message, s"${server.ip.ip}:${server.port}", modeName, message.mapname)
      val duelAccumulation = DuelAccumulation(Queue.empty)
      TransitionalBetterDuel(
        gameHeader = gameHeader,
        duelAccumulation = duelAccumulation,
        isRunning = !message.gamepaused,
        timeRemaining = SecondsRemaining(message.remain)
      )
    }
  }

  val EXPECTED_TIME_SECONDS = 540

}

sealed trait DuelParseError {
  def message = toString
}
object DuelParseError {

  case object InputNotConvertedServerInfoReply extends DuelParseError

  case class ExpectedMoreThan2Clients(got: Int) extends DuelParseError

  case object NonDuelMode extends DuelParseError

  case class RemainingTimeExpected(expected: Int, got: Int) extends DuelParseError

  case class ExpectedExactly2Players(got: List[String]) extends DuelParseError

  case object CouldNotFindLogItemToSayAllPlayersStarted extends DuelParseError

  case object CouldNotFindProofThatThePlayersFinished extends DuelParseError

  case object PlayersDisappeared extends DuelParseError

  case class FoundModeRejecting(mode: String) extends DuelParseError

  case class Expected8MinutesToDuel(foundMinutes: Int, foundSeconds: Int) extends DuelParseError

  case class EfficiencyExpectFragsOver10(found: List[Int]) extends DuelParseError
  case class InstaExpectFragsOver20(found: List[Int]) extends DuelParseError
  case class FFAExpectSum15(have: Int) extends DuelParseError

  case object PlayerLogEmpty extends DuelParseError

}
