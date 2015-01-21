package gg.duel.tourney

object SingleElimination extends App {
//
//  sealed trait Game
//
//  sealed trait SlotState
//  case class AwaitingGameWinner(game: Game) extends SlotState
//  case class AwaitingGameLoser(game: Game) extends SlotState
//  case class Filled(user: String) extends SlotState
//  case object Unfilled extends SlotState
//
//  class Slot(var slotState: SlotState = Unfilled)
//
//  sealed trait GameState
//  case object AwaitingResult extends GameState
//  case object NoResult extends GameState
//  case object NotWon extends GameState
//  case class WinnerLoser(winner: String, loser: String) extends GameState
//
//  class SingleEliminationGame(val firstSlot: Slot, val secondSlot: Slot, var gameState: GameState, var winnerFills: Slot) extends Game
//  class DoubleEliminationGame(val firstSlot: Slot, val secondSlot: Slot, var gameState: GameState, var winnerFills: Slot, var loserFills: Slot) extends Game
//
//  case class Tournament(slots: List[Slot], games: List[Game], winningSlot: Slot)
//
//  def powerSingleEliminationTournament(power: Int) = {
//
//    val slots = (0 to (Math.pow(2, power + 1).toInt-2)).map(_ => new Slot(Unfilled)).toList
//
//    val games = (0 to (Math.pow(2, power) - 1).toInt).toList.map {
//      gameNumber =>
//        new SingleEliminationGame(
//          firstSlot = slots(2 * gameNumber),
//          secondSlot = slots(1 + 2 * gameNumber),
//          gameState = AwaitingResult,
//          winnerFills = slots(gameNumber + Math.pow(2, power + 1).toInt)
//        )
//    }
//
//    Tournament(slots, games, winningSlot = slots.last)
//
//  }
//
//  def doubleElimination(power: Int) = {
//    def produceGameSet(currentPower: Int): (List[])
//  }

}