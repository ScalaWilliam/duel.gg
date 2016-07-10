package gg.duel.pinger.analytics

import akka.util.ByteString
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZFoundDuel, ZIteratorState, ZOutOfGameState}
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{Extractor, SauerBytes, Server}

import scala.util.Try

object MultiplexedReader {
  /** ParsedMessage ==> MFoundGame(_, CompletedDuel) **/
  type MProcessor = ParsedMessage => MIteratorState
  sealed trait MIteratorState {
    def next: MProcessor = {
      case parsedMessage @ ParsedMessage(server, _, _) =>
        serverStates.get(server) match {
          case Some(state) =>
            state.next.apply(parsedMessage) match {
              case nextState @ ZFoundDuel(_, completedDuel) =>
                MFoundGame(serverStates.updated(server, nextState), CompletedGame(completedDuel), Option(server, nextState))
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
  def sauerBytesToParsedMessages(sauerBytes: SauerBytes): List[ParsedMessage] = {
    val byteString = ByteString(sauerBytes.message.toArray)
    for {
      parsedObjects <- Try(Extractor.extractDuel(byteString)).toOption.toList
      parsedObject <- parsedObjects
    } yield ParsedMessage(sauerBytes.server, sauerBytes.time, parsedObject)
  }
  def multiplexParsedMessagesStates(parsedMessages: Iterator[ParsedMessage]): Iterator[MIteratorState] = {
    parsedMessages.scanLeft(MInitial: MIteratorState)(_.next(_))
  }
  /** SauerBytes ==> SFoundGame(_, Seq(CompletedDuel)) **/
  type SProcessor = SauerBytes => SIteratorState
  sealed trait SIteratorState {
    def next: SProcessor = {
      case sauerBytes =>
        /** Note: Multiple parsedMessages from a single SauerBytes CANNOT lead to a CompletedDuel. Impossibru. **/
        /** We're short-circuiting here! **/
        sauerBytesToParsedMessages(sauerBytes).foldLeft(mIteratorState)(_.next(_)) match {
          case state @ MFoundGame(_, game, _) => SFoundGame(state, game)
          case state => SProcessing(state)
        }
    }
    def mIteratorState: MIteratorState
  }
  object SIteratorState {
    def empty: SIteratorState = SInitial
  }
  case object SInitial extends SIteratorState {
    override val mIteratorState = MInitial
  }
  case class CompletedGame(game: SimpleCompletedDuel, metaId: Option[String] = None)
  case class SFoundGame(mIteratorState: MIteratorState, completedGame: CompletedGame) extends SIteratorState
  case class SProcessing(mIteratorState: MIteratorState) extends SIteratorState
  def multiplexSecond(inputs: Iterator[SauerBytes]): Iterator[CompletedGame] = {
    inputs.scanLeft(SInitial: SIteratorState)(_.next(_)).collect {
      case SFoundGame(_, completedGame) => completedGame
    }
  }
}

