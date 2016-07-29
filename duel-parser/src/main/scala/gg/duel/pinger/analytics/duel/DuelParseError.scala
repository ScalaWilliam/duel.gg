package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.DuelParseError.Multiple
import org.scalactic.Every

/**
  * Created by me on 29/07/2016.
  */
object DuelParseError {

  def mapEvery[T](e: Every[DuelParseError]): DuelParseError = {
    Multiple.dpe(e: _*)
  }

  case class Multiple(errors: DuelParseError*) extends DuelParseError

  object Multiple {
    def dpe(items: DuelParseError*): DuelParseError = {
      Multiple(items.flatMap {
        case m: Multiple => m.errors
        case other => List(other)
      }: _*)
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

sealed trait DuelParseError {
  def message = toString

  def toList: List[DuelParseError] = {
    this match {
      case Multiple(first, rest@_*) => (first :: rest.toList).flatMap(_.toList)
      case other => List(other)
    }
  }
}
