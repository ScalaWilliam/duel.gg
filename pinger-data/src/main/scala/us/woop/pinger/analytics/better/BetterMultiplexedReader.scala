package us.woop.pinger.analytics.better

import us.woop.pinger.analytics.better.StreamedSimpleDuelMaker.{ZIteratorState, ZOutOfDuelState, ZFoundGame}
import us.woop.pinger.analytics.worse.{MultiplexedDuelReader, DuelMaker}
import DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.Server
import us.woop.pinger.data.journal.SauerBytes

object BetterMultiplexedReader {

  def multiplex(serverBytes: Iterator[SauerBytes]): Iterator[SimpleCompletedDuel] = {
    MultiplexedDuelReader.sauerBytesIToParsedMessagesI(serverBytes).scanLeft(MInitial: MIteratorState)(_.next(_)).collect{ case MFoundGame(_, completedDuel) => completedDuel }
  }

  def multiplexParsedMessages(parsedMessages: Iterator[ParsedMessage]): Iterator[SimpleCompletedDuel] = {
    multiplexParsedMessagesStates(parsedMessages).collect{ case MFoundGame(_, completedDuel) => completedDuel }
  }

  def multiplexParsedMessagesStates(parsedMessages: Iterator[ParsedMessage]): Iterator[MIteratorState] = {
    parsedMessages.scanLeft(MInitial: MIteratorState)(_.next(_))
  }

  /** ParsedMessage ==> MFoundGame(_, CompletedDuel) **/
  type MProcessor = ParsedMessage => MIteratorState
  trait MIteratorState {
    def next: MProcessor = {
      case parsedMessage @ ParsedMessage(server, _, _) =>
        serverStates.get(server) match {
          case Some(state) =>
            state.next.apply(parsedMessage) match {
              case nextState @ ZFoundGame(_, completedDuel) =>
                MFoundGame(serverStates.updated(server, nextState), completedDuel)
              case nextState =>
                MProcessing(serverStates.updated(server, nextState))
            }
          case None =>
            MProcessing(serverStates.updated(server, ZOutOfDuelState)).next.apply(parsedMessage)
        }
    }
    def serverStates: Map[Server, ZIteratorState]
  }
  case class MProcessing(serverStates: Map[Server, ZIteratorState]) extends MIteratorState
  case object MInitial extends MIteratorState {
    override val serverStates = Map.empty[Server, ZIteratorState]
  }
  case class MFoundGame(serverStates: Map[Server, ZIteratorState], completedDuel: SimpleCompletedDuel) extends MIteratorState

  /** SauerBytes ==> SFoundGame(_, Seq(CompletedDuel)) **/
  type SProcessor = SauerBytes => SIteratorState
  trait SIteratorState {
    def next: SProcessor = {
      case sauerBytes =>
        /** Note: Multiple parsedMessages from a single SauerBytes CANNOT lead to a CompletedDuel. Impossibru. **/
        /** We're short-circuiting here! **/
        MultiplexedDuelReader.sauerBytesToParsedMessages(sauerBytes).scanLeft(mIteratorState)(_.next(_)).lastOption match {
          case Some(state @ MFoundGame(_, game)) => SFoundGame(state, game)
          case Some(state) => SProcessing(state)
          case None => SProcessing(mIteratorState)
        }
    }
    def mIteratorState: MIteratorState
  }
  case object SInitial extends SIteratorState {
    override val mIteratorState = MInitial
  }
  case class SFoundGame(mIteratorState: MIteratorState, completedDuel: SimpleCompletedDuel) extends SIteratorState
  case class SProcessing(mIteratorState: MIteratorState) extends SIteratorState
  def multiplexSecond(inputs: Iterator[SauerBytes]): Iterator[SimpleCompletedDuel] = {
    inputs.scanLeft(SInitial: SIteratorState)(_.next(_)).collect {
      case SFoundGame(_, completedDuel) => completedDuel
    }
  }
}

