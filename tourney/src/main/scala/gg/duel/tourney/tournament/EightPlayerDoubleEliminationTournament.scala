package gg.duel.tourney.tournament

/**
 * Created by William on 22/01/2015.
 */
class EightPlayerDoubleEliminationTournament(val players: List[String]) extends Tournament {
  override val tournamentType = "double-elimination-eight-players"
  games += new Game(1, Slot.filled(players(0)), Slot.filled(players(1)))
  games += new Game(2, Slot.filled(players(2)), Slot.filled(players(3)))
  games += new Game(3, Slot.filled(players(4)), Slot.filled(players(5)))
  games += new Game(4, Slot.filled(players(6)), Slot.filled(players(7)))
  games += new Game(5, Slot.loserOf(games(1)), Slot.loserOf(games(2)))
  games += new Game(6, Slot.loserOf(games(3)), Slot.loserOf(games(4)))
  games += new Game(7, Slot.winnerOf(games(1)), Slot.loserOf(games(2)))
  games += new Game(8, Slot.winnerOf(games(3)), Slot.loserOf(games(4)))
  games += new Game(9, Slot.loserOf(games(8)), Slot.winnerOf(games(5)))
  games += new Game(10, Slot.winnerOf(games(6)), Slot.loserOf(games(7)))
  games += new Game(11, Slot.winnerOf(games(7)), Slot.winnerOf(games(8)))
  games += new Game(12, Slot.winnerOf(games(9)), Slot.winnerOf(games(10)))
  games += new Game(13, Slot.winnerOf(games(12)), Slot.loserOf(games(11)))
  games += new Game(14, Slot.winnerOf(games(11)), Slot.winnerOf(games(13)))
  val winnerSet = Set(1, 2, 3, 4, 7, 8, 11, 14).map(games.apply)
  val losersSet = Set(5,6,9,10,12,13,14).map(games.apply)
  override val finalGame = games(14)
}
