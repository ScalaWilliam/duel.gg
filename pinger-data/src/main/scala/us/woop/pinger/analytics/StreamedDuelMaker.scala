package us.woop.pinger.analytics

import us.woop.pinger.analytics.DuelMaker.{DuelState, CompletedDuel}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import org.scalactic._

object StreamedDuelMaker {
  type Processor = ParsedMessage => ZIteratorState
  trait ZIteratorState {
    def next: Processor
  }
  trait GoesToOutOfState {
    this: ZIteratorState =>
      override def next = ZOutOfDuelState.next
  }
  case class ZFoundGame(completedDuel: CompletedDuel) extends ZIteratorState with GoesToOutOfState
  case class ZInDuelState(duelState: DuelState) extends ZIteratorState {
    override def next = parsedMessage =>
      duelState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedDuelState(cause)
        case Good(completedDuel: CompletedDuel) =>
          new ZFoundGame(completedDuel) {
            override def next = ZOutOfDuelState.next(parsedMessage).next
          }
        case Good(transitionalDuel) =>
          ZInDuelState(transitionalDuel)
      }
  }
  case class ZRejectedDuelState(cause: Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case object ZOutOfDuelState extends ZIteratorState {
    override def next: Processor =
      DuelMaker.Duel.beginDuelParsedMessage(_).fold(ZInDuelState, ZRejectedDuelState)
  }

  def parsedToState(iterator: Iterator[ParsedMessage]): Iterator[ZIteratorState] =
    iterator.scanLeft(ZOutOfDuelState:ZIteratorState)(_.next(_))

  object IteratorImplicit {
    implicit class onIterator(iterator: Iterator[ParsedMessage]) {
      def asStateIterator = parsedToState(iterator)
    }
  }

}
