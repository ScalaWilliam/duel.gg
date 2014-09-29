package us.woop.pinger.analytics.worse

import akka.util.ByteString
import StreamedDuelMaker.{ZFoundGame, ZIteratorState, ZOutOfDuelState}
import us.woop.pinger.analytics.worse.DuelMaker.CompletedDuel
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.journal.SauerBytes
import us.woop.pinger.data.{Extractor, Server}

import scala.util.Try

object MultiplexedDuelReader {

  object ImplicitDuelReader {
    implicit class addMultiplex(serverBytes: Iterator[SauerBytes]) {
      /** Both alternate implementations, doing the same thing **/
      def toCompletedDuels: Iterator[CompletedDuel] =
        multiplex(serverBytes)
      def toCompletedDuelsSecond: Iterator[CompletedDuel] =
        multiplexSecond(serverBytes)
    }
  }

  def sauerBytesToParsedMessages(sauerBytes: SauerBytes): List[ParsedMessage] = {
    val byteString = ByteString(sauerBytes.message.toArray)
    for {
      parsedObjects <- Try(Extractor.extract(byteString)).toOption.toList
      parsedObject <- parsedObjects
    } yield ParsedMessage(sauerBytes.server, sauerBytes.time, parsedObject)
  }

  def sauerBytesIToParsedMessagesI(serverBytes: Iterator[SauerBytes]): Iterator[ParsedMessage] = {
    serverBytes.flatMap(sauerBytesToParsedMessages)
  }

  def multiplex(serverBytes: Iterator[SauerBytes]): Iterator[CompletedDuel] = {
    sauerBytesIToParsedMessagesI(serverBytes).scanLeft(MInitial: MIteratorState)(_.next(_)).collect{ case MFoundGame(_, completedDuel) => completedDuel }
  }

  def multiplexParsedMessages(parsedMessages: Iterator[ParsedMessage]): Iterator[CompletedDuel] = {
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
              case nextState @ ZFoundGame(completedDuel) =>
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
  case class MFoundGame(serverStates: Map[Server, ZIteratorState], completedDuel: CompletedDuel) extends MIteratorState

  /** SauerBytes ==> SFoundGame(_, Seq(CompletedDuel)) **/
  type SProcessor = SauerBytes => SIteratorState
  trait SIteratorState {
    def next: SProcessor = {
      case sauerBytes =>
        /** Note: Multiple parsedMessages from a single SauerBytes CANNOT lead to a CompletedDuel. Impossibru. **/
        /** We're short-circuiting here! **/
        sauerBytesToParsedMessages(sauerBytes).scanLeft(mIteratorState)(_.next(_)).lastOption match {
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
  case class SFoundGame(mIteratorState: MIteratorState, completedDuel: CompletedDuel) extends SIteratorState
  case class SProcessing(mIteratorState: MIteratorState) extends SIteratorState
  def multiplexSecond(inputs: Iterator[SauerBytes]): Iterator[CompletedDuel] = {
    inputs.scanLeft(SInitial: SIteratorState)(_.next(_)).collect {
      case SFoundGame(_, completedDuel) => completedDuel
    }
  }
}
