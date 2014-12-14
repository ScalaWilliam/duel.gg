package us.woop.pinger.analytics.better

import org.scalactic.{Bad, Every, Good}
import us.woop.pinger.analytics.CTFGameMaker
import us.woop.pinger.analytics.CTFGameMaker.{SimpleCompletedCTF, CtfFound, CtfState}
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
  case class ZRejectedGameState(lastMessage: Option[ParsedMessage], cause: Every[org.scalactic.ErrorMessage]) extends ZIteratorState with GoesToOutOfState
  case object ZOutOfGameState extends ZIteratorState {
    override def lastMessage = None
    override def next: Processor = m => {
      val duelAttempt = BetterDuelMaker.Duel.beginDuelParsedMessage(m).map(ZInDuelState.apply(Some(m), _))
      val ctfAttempt = CTFGameMaker.CTFGame.beginCTFParsing(m).map(ZInCtfState.apply(Some(m), _))
      (duelAttempt, ctfAttempt) match {
        case (Good(duel), _) => duel
        case (_, Good(ctf)) => ctf
        case (Bad(a), Bad(b)) => ZRejectedGameState(Some(m), a ++ b)
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
