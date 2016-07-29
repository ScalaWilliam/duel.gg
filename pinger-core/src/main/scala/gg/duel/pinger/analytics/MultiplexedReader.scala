package gg.duel.pinger.analytics

import akka.util.ByteString
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZFoundDuel, ZIteratorState, ZOutOfGameState}
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{Extractor, SauerBytes, Server}

import scala.util.control.NonFatal

sealed trait ServerStates {
  def updated(server: Server, zIteratorState: ZIteratorState): ServerStates

  def get(server: Server): Option[ZIteratorState]
}

object ServerStates {
  case class ServerStatesImmutable(map: Map[Server, ZIteratorState]) extends ServerStates {
    def updated(server: Server, zIteratorState: ZIteratorState): ServerStates =
      copy(map = map.updated(server, zIteratorState))

    def get(server: Server): Option[ZIteratorState] = map.get(server)
  }

  class ServerStatesMutable extends ServerStates {
    private val map: java.util.Map[Server, ZIteratorState] = new java.util.HashMap()

    override def updated(server: Server, zIteratorState: ZIteratorState): ServerStates = {
      map.put(server, zIteratorState)
      this
    }

    override def get(server: Server): Option[ZIteratorState] = {
      Option(map.get(server))
    }
  }

  def immutable: ServerStates = ServerStatesImmutable(Map.empty)

  def mutable: ServerStates = new ServerStatesMutable
}
object MultiplexedReader {


  /** ParsedMessage ==> MFoundGame(_, CompletedDuel) **/
  type MProcessor = ParsedMessage => MIteratorState

  sealed trait MIteratorState {
    def next: MProcessor = {
      case parsedMessage@ParsedMessage(server, _, _) =>
        serverStates.get(server) match {
          case Some(state) =>
            state.next.apply(parsedMessage) match {
              case nextState@ZFoundDuel(_, completedDuel) =>
                MFoundGame(serverStates.updated(server, nextState), CompletedGame(completedDuel), Some(server, nextState))
              case nextState =>
                MProcessing(serverStates.updated(server, nextState), Some(server, nextState))
            }
          case None =>
            MProcessing(serverStates.updated(server, ZOutOfGameState), Some(server, ZOutOfGameState)).next.apply(parsedMessage)
        }
    }

    def serverStates: ServerStates

    def lastUpdatedState: Option[(Server, ZIteratorState)]
  }

  case class MProcessing(serverStates: ServerStates, lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState

  case object MInitial extends MIteratorState {
    override val serverStates = ServerStates.mutable
    override val lastUpdatedState = None
  }

  case class MFoundGame(serverStates: ServerStates, completedGame: CompletedGame, lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState

  val messageCache = new ThreadLocal[java.util.Map[Server, (SauerBytes, List[ParsedMessage])]] {
    override def initialValue(): java.util.Map[Server, (SauerBytes, List[ParsedMessage])] = new java.util.HashMap()
  }

  private def sauerBytesToParsedMessages(sauerBytes: SauerBytes): List[ParsedMessage] = {
    val result = try {
      Extractor
        .extractDuel
        .applyOrElse(sauerBytes.message, (_: ByteString) => Nil)
        .map(parsedObject => ParsedMessage(sauerBytes.server, sauerBytes.time, parsedObject))
    } catch {
      case NonFatal(e) => List.empty
    }
    result
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
          case state@MFoundGame(_, game, _) => SFoundGame(state, game)
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

