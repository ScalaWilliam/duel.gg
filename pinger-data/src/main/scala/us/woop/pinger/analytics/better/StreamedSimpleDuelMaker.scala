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
    def lastMessage: Option[ParsedMessage]
  }
  trait GoesToOutOfState {
    this: ZIteratorState =>
    override def next = ZOutOfDuelState.next
  }
  case class ZFoundGame(lastMessage: Option[ParsedMessage], completedDuel: SimpleCompletedDuel) extends ZIteratorState with GoesToOutOfState
  case class ZInDuelState(lastMessage: Option[ParsedMessage], duelState: BetterDuelState) extends ZIteratorState {
    override def next = parsedMessage =>
      duelState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedDuelState(Some(parsedMessage), cause)
        case Good(BetterDuelFound(header, completedDuel)) =>
          new ZFoundGame(Some(parsedMessage), completedDuel) {
            override def next = ZOutOfDuelState.next(parsedMessage).next
          }
        case Good(transitionalDuel) =>
          ZInDuelState(Some(parsedMessage), transitionalDuel)
      }
  }
  case class ZRejectedDuelState(lastMessage: Option[ParsedMessage], cause: Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case object ZOutOfDuelState extends ZIteratorState {
    override def lastMessage = None
    override def next: Processor = m =>
      BetterDuelMaker.Duel.beginDuelParsedMessage(m).fold(ZInDuelState.apply(Some(m), _), ZRejectedDuelState.apply(Some(m), _))
  }

  def parsedToState(iterator: Iterator[ParsedMessage]): Iterator[ZIteratorState] =
    iterator.scanLeft(ZOutOfDuelState:ZIteratorState)(_.next(_))

  object IteratorImplicit {
    implicit class onIterator(iterator: Iterator[ParsedMessage]) {
      def asStateIterator = parsedToState(iterator)
    }
  }

}
