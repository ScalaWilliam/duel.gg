package us.woop.pinger.analytics.better

import us.woop.pinger.analytics.CTFGameMaker.SimpleCompletedCTF
import us.woop.pinger.analytics.better.StreamedSimpleDuelMaker.{ZFoundCtf, ZIteratorState, ZOutOfGameState, ZFoundDuel}
import us.woop.pinger.analytics.worse.{MultiplexedDuelReader, DuelMaker}
import DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.Server
import us.woop.pinger.data.journal.SauerBytes

object BetterMultiplexedReader {

//  def multiplex(serverBytes: Iterator[SauerBytes]): Iterator[SimpleCompletedDuel] = {
//    MultiplexedDuelReader.sauerBytesIToParsedMessagesI(serverBytes).scanLeft(MInitial: MIteratorState)(_.next(_)).collect{ case MFoundGame(_, completedDuel, _) => completedDuel }
//  }

//  def multiplexParsedMessages(parsedMessages: Iterator[ParsedMessage]): Iterator[SimpleCompletedDuel] = {
//    multiplexParsedMessagesStates(parsedMessages).collect{ case MFoundGame(_, completedDuel, _) => completedDuel }
//  }

//  def multiplexParsedMessagesStates(parsedMessages: Iterator[ParsedMessage]): Iterator[MIteratorState] = {
//    parsedMessages.scanLeft(MInitial: MIteratorState)(_.next(_))
//  }

  /** ParsedMessage ==> MFoundGame(_, CompletedDuel) **/
  type MProcessor = ParsedMessage => MIteratorState
  trait MIteratorState {
    def next: MProcessor = {
      case parsedMessage @ ParsedMessage(server, _, _) =>
        serverStates.get(server) match {
          case Some(state) =>
            state.next.apply(parsedMessage) match {
              case nextState @ ZFoundDuel(_, completedDuel) =>
                MFoundGame(serverStates.updated(server, nextState), CompletedGame(Left(completedDuel)), Option(server, nextState))
              case nextState @ ZFoundCtf(_, completedCtf) =>
                MFoundGame(serverStates.updated(server, nextState), CompletedGame(Right(completedCtf)), Option(server, nextState))
              case nextState =>
                MProcessing(serverStates.updated(server, nextState), Option(server, nextState))
            }
          case None =>
            MProcessing(serverStates.updated(server, ZOutOfGameState), Option(server, ZOutOfGameState)).next.apply(parsedMessage)
        }
    }
    def serverStates: Map[Server, ZIteratorState]
    def lastUpdatedState: Option[(Server, ZIteratorState)]
  }
  case class MProcessing(serverStates: Map[Server, ZIteratorState], lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState
  case object MInitial extends MIteratorState {
    override val serverStates = Map.empty[Server, ZIteratorState]
    override val lastUpdatedState = None
  }

  case class MFoundGame(serverStates: Map[Server, ZIteratorState], completedGame: CompletedGame, lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState

  /** SauerBytes ==> SFoundGame(_, Seq(CompletedDuel)) **/
  type SProcessor = SauerBytes => SIteratorState
  trait SIteratorState {
    def next: SProcessor = {
      case sauerBytes =>
        /** Note: Multiple parsedMessages from a single SauerBytes CANNOT lead to a CompletedDuel. Impossibru. **/
        /** We're short-circuiting here! **/
        MultiplexedDuelReader.sauerBytesToParsedMessages(sauerBytes).scanLeft(mIteratorState)(_.next(_)).lastOption match {
          case Some(state @ MFoundGame(_, game, _)) => SFoundGame(state, game)
          case Some(state) => SProcessing(state)
          case None => SProcessing(mIteratorState)
        }
    }
    def mIteratorState: MIteratorState
  }
  case object SInitial extends SIteratorState {
    override val mIteratorState = MInitial
  }
  case class CompletedGame(game: Either[SimpleCompletedDuel, SimpleCompletedCTF], metaId: Option[String] = None)
  case class SFoundGame(mIteratorState: MIteratorState, completedGame: CompletedGame) extends SIteratorState
  case class SProcessing(mIteratorState: MIteratorState) extends SIteratorState
  def multiplexSecond(inputs: Iterator[SauerBytes]): Iterator[CompletedGame] = {
    inputs.scanLeft(SInitial: SIteratorState)(_.next(_)).collect {
      case SFoundGame(_, completedGame) => completedGame
    }
  }
}

