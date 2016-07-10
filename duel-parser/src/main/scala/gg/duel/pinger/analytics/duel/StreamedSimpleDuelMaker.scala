package gg.duel.pinger.analytics.duel

import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import org.scalactic.{Bad, Every, Good}

object StreamedSimpleDuelMaker {

  type Processor = ParsedMessage => ZIteratorState
  trait ZIteratorState {
    def next: Processor
    def lastMessage: Option[ParsedMessage]
  }
  trait GoesToOutOfState {
    this: ZIteratorState =>
    override def next = ZOutOfGameState.next
  }
  case class ZFoundDuel(lastMessage: Option[ParsedMessage], completedDuel: SimpleCompletedDuel) extends ZIteratorState with GoesToOutOfState

  case class ZInDuelState(lastMessage: Option[ParsedMessage], duelState: BetterDuelState) extends ZIteratorState {
    override def next = parsedMessage =>
      duelState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedDuelState(Some(parsedMessage), cause)
        case Good(BetterDuelFound(header, completedDuel)) =>
          new ZFoundDuel(Some(parsedMessage), completedDuel) {
            override def next = ZOutOfGameState.next(parsedMessage).next
          }
        case Good(transitionalDuel) =>
          ZInDuelState(Some(parsedMessage), transitionalDuel)
      }
  }
  case class ZRejectedDuelState(lastMessage: Option[ParsedMessage], cause: Every[DuelParseError]) extends ZIteratorState with GoesToOutOfState
  case class ZRejectedGameState(lastMessage: Option[ParsedMessage], cause: Every[DuelParseError], duelCause: Every[DuelParseError]) extends ZIteratorState with GoesToOutOfState
  case object ZOutOfGameState extends ZIteratorState {
    override def lastMessage = None
    override def next: Processor = m => {
      Duel.beginDuelParsedMessage(m).map(ZInDuelState.apply(Some(m), _)) match {
        case Good(duel) => duel
        case Bad(duelCause) => ZRejectedGameState(Some(m), cause = duelCause, duelCause = duelCause)
      }
    }
  }
  def parsedToState(iterator: Iterator[ParsedMessage]): Iterator[ZIteratorState] =
    iterator.scanLeft(ZOutOfGameState:ZIteratorState)(_.next(_))
  object IteratorImplicit {
    implicit class onIterator(iterator: Iterator[ParsedMessage]) {
      def asStateIterator = parsedToState(iterator)
    }
  }

}
