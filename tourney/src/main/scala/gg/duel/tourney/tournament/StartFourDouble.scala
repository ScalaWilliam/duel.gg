package gg.duel.tourney.tournament

/**
 * Created by William on 22/01/2015.
 */
class StartFourDouble(val players: List[String]) extends Tournament {
  override val tournamentType = "double-elimination-four-players"
  games += new Game(1, Slot.filled(players(0)), Slot.filled(players(3)))
  games += new Game(2, Slot.filled(players(1)), Slot.filled(players(2)))
  games += new Game(3, Slot.winnerOf(games(1)), Slot.winnerOf(games(2)))
  games += new Game(4, Slot.loserOf(games(1)), Slot.loserOf(games(2)))
  games += new Game(5, Slot.winnerOf(games(4)), Slot.loserOf(games(3)))
  games += new Game(6, Slot.winnerOf(games(3)), Slot.winnerOf(games(5)))
  override val finalGame = games(6)
}
