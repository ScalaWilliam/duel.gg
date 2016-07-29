package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.MIteratorState.{MFoundGame, MInitial}
import gg.duel.pinger.analytics.SIteratorState.{SFoundGame, SProcessing}
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.data.SauerBytes

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

      /** Note: Multiple parsedMessages from a single SauerBytes CANNOT lead to a CompletedDuel. Impossibru. **/
      /** We're short-circuiting here! **/
      MultiplexedReader.sauerBytesToParsedMessages(sauerBytes).foldLeft(mIteratorState)(_.next(_)) match {
        case state@MFoundGame(_, game, _) => SFoundGame(state, game)
        case state => SProcessing(state)
      }
  }

  def mIteratorState: MIteratorState
}
