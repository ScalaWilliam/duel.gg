package us.woop.pinger.analytics.better

import org.scalactic.{Bad, Every, Good}
import us.woop.pinger.analytics.better.BetterDuelMaker.{BetterDuelFound, BetterDuelState}
import us.woop.pinger.analytics.worse.DuelMaker
import us.woop.pinger.analytics.worse.DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.ParsedPongs.ParsedMessage

object StreamedSimpleDuelMaker {

  type Processor = ParsedMessage => ZIteratorState
  trait ZIteratorState {
    def next: Processor
  }
  trait GoesToOutOfState {
    this: ZIteratorState =>
    override def next = ZOutOfDuelState.next
  }
  case class ZFoundGame(completedDuel: SimpleCompletedDuel) extends ZIteratorState with GoesToOutOfState
  case class ZInDuelState(duelState: BetterDuelState) extends ZIteratorState {
    override def next = parsedMessage =>
      duelState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedDuelState(cause)
        case Good(BetterDuelFound(header, completedDuel)) =>
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
      BetterDuelMaker.Duel.beginDuelParsedMessage(_).fold(ZInDuelState, ZRejectedDuelState)
  }

  def parsedToState(iterator: Iterator[ParsedMessage]): Iterator[ZIteratorState] =
    iterator.scanLeft(ZOutOfDuelState:ZIteratorState)(_.next(_))

  object IteratorImplicit {
    implicit class onIterator(iterator: Iterator[ParsedMessage]) {
      def asStateIterator = parsedToState(iterator)
    }
  }

}
