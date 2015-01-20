package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.ctf.{CTFGame, CtfState, CtfFound}
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
  case class ZFoundCtf(lastMessage: Option[ParsedMessage], completedDuel: SimpleCompletedCTF) extends ZIteratorState with GoesToOutOfState

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
  case class ZInCtfState(lastMessage: Option[ParsedMessage], ctfState: CtfState) extends ZIteratorState {
    override def next = parsedMessage =>
      ctfState.next(parsedMessage) match {
        case Bad(cause) => ZRejectedCtfState(Some(parsedMessage), cause)
        case Good(CtfFound(header, completedCtf)) =>
          new ZFoundCtf(Some(parsedMessage), completedCtf) {
            override def next = ZOutOfGameState.next(parsedMessage).next
          }
        case Good(transitionalCtf) =>
          ZInCtfState(Some(parsedMessage), transitionalCtf)
      }
  }
  case class ZRejectedDuelState(lastMessage: Option[ParsedMessage], cause: Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case class ZRejectedCtfState(lastMessage: Option[ParsedMessage], cause: Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case class ZRejectedGameState(lastMessage: Option[ParsedMessage], cause: Every[org.scalactic.ErrorMessage], duelCause: Every[org.scalactic.ErrorMessage], ctfCause:Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case object ZOutOfGameState extends ZIteratorState {
    override def lastMessage = None
    override def next: Processor = m => {
      val duelAttempt = Duel.beginDuelParsedMessage(m).map(ZInDuelState.apply(Some(m), _))
      val ctfAttempt = CTFGame.beginCTFParsing(m).map(ZInCtfState.apply(Some(m), _))
      (duelAttempt, ctfAttempt) match {
        case (Good(duel), _) => duel
        case (_, Good(ctf)) => ctf
        case (Bad(duelCause), Bad(ctfCause)) => ZRejectedGameState(Some(m), cause = duelCause ++ ctfCause, duelCause, ctfCause)
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
