package gg.duel.pinger.analytics.duel

import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import org.scalactic.{Bad, Good}

/**
  * Created by me on 29/07/2016.
  */

sealed trait ZIteratorState {
  def next: ParsedMessage => ZIteratorState

  def lastMessage: Option[ParsedMessage]
}

object ZIteratorState {

  sealed trait GoesToOutOfState extends ZIteratorState {
    override def next = ZOutOfGameState.next
  }

  case class ZFoundDuel(lastMessage: Option[ParsedMessage], completedDuel: SimpleCompletedDuel) extends ZIteratorState with GoesToOutOfState

  case class ZInDuelState(lastMessage: Option[ParsedMessage], duelState: BetterDuelState) extends ZIteratorState {
    override def next = parsedMessage =>
      duelState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedDuelState(Some(parsedMessage), DuelParseError.mapEvery(cause))
        case Good(BetterDuelFound(header, completedDuel)) =>
          new ZFoundDuel(Some(parsedMessage), completedDuel) {
            override def next = ZOutOfGameState.next(parsedMessage).next
          }
        case Good(transitionalDuel) =>
          ZInDuelState(Some(parsedMessage), transitionalDuel)
      }
  }

  case class ZRejectedDuelState(lastMessage: Option[ParsedMessage], cause: DuelParseError) extends ZIteratorState with GoesToOutOfState

  case object ZOutOfGameState extends ZIteratorState {
    override def lastMessage = None

    override def next: ParsedMessage => ZIteratorState = m => {
      Duel.beginDuelParsedMessage(m).map(ZInDuelState.apply(Some(m), _)) match {
        case Good(duel) => duel
        case Bad(duelCause) => ZRejectedDuelState(Some(m), cause = duelCause)
      }
    }
  }

}
