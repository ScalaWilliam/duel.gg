package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.DuelParseError._
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic._

import scala.collection.immutable.Queue

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
        Bad(Multiple.dpe(Array(a, b, c).flatMap(_.toEither.left.toSeq): _*))
    }
  }

  val EXPECTED_TIME_SECONDS = 540

}

sealed trait DuelParseError {
  def message = toString

  def toList: List[DuelParseError] = {
    this match {
      case Multiple(first, rest @ _*) => (first :: rest.toList).flatMap(_.toList)
      case other => List(other)
    }
  }
}

object DuelParseError {

  def mapEvery[T](e: Every[DuelParseError]): DuelParseError = {
    Multiple.dpe(e: _*)
  }

  case class Multiple(first: DuelParseError, errors: DuelParseError*) extends DuelParseError

  object Multiple {
    def dpe(items: DuelParseError*): DuelParseError = {
      items.flatMap { case Multiple(a, rest@_*) => a +: rest; case o => Seq(o) } match {
        case Seq(first, rest@_*) => Multiple(first, rest: _*)
        case Seq(first) => first
      }
    }
  }

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
