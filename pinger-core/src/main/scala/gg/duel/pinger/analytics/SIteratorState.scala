package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.MIteratorState.{MFoundGame, MInitial}
import gg.duel.pinger.analytics.SIteratorState.{SFoundGame, SProcessing}
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.analytics.duel.ZIteratorState.{ZOutOfGameState, ZRejectedDuelState}
import gg.duel.pinger.data.{PongParser, SauerBytes}

/**
  * Created by me on 29/07/2016.
  */
object SIteratorState {

  def empty: SIteratorState = SInitial

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

sealed trait SIteratorState {
  def next: SauerBytes => SIteratorState = {
    case sauerBytes =>

      def ignore2 = PongParser.GetServerInfoReply.isServerInfoReply(sauerBytes.message) &&
        PongParser.GetServerInfoReply.hasNoPlayers(sauerBytes.message)

      // OPTIMIZATION: if we're out of a duel then there's no good reason to parse the relaxed info at all.
      def ignore = PongParser.GetRelaxedPlayerExtInfo.isRelaxedInfo(sauerBytes.message) &&
        mIteratorState.serverStates.get(sauerBytes.server).exists {
          case _: ZRejectedDuelState => true
          case ZOutOfGameState => true
          case _ => false
        }

      if (ignore2 || ignore) SProcessing(mIteratorState.step)
      else {
        MultiplexedReader.sauerBytesToParsedMessages(sauerBytes) match {
          case Some(message) => mIteratorState.next(message) match {
            case state@MFoundGame(_, game, _) => SFoundGame(state, game)
            case state => SProcessing(state)
          }
          case None => SProcessing(mIteratorState.step)
        }
      }
  }

  def mIteratorState: MIteratorState
}
