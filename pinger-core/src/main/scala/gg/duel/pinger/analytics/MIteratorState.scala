package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.MIteratorState.{MFoundGame, MProcessing}
import gg.duel.pinger.analytics.SIteratorState.CompletedGame
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZFoundDuel, ZIteratorState, ZOutOfGameState}
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.Server

/**
  * Created by me on 29/07/2016.
  */
object MIteratorState {

  case class MProcessing(serverStates: ServerStates, lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState

  case object MInitial extends MIteratorState {
    override val serverStates = ServerStates.mutable
    override val lastUpdatedState = None
  }

  case class MFoundGame(serverStates: ServerStates, completedGame: CompletedGame, lastUpdatedState: Option[(Server, ZIteratorState)]) extends MIteratorState

}

sealed trait MIteratorState {
  def next: ParsedMessage => MIteratorState = {
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
