package gg.duel.tourney.tournament

class SixteenPlayerDoubleEliminationTournament(val players: List[String]) extends Tournament {
  games += new Game(1, Slot.filled(players(0)), Slot.filled(players(1)))
  games += new Game(2, Slot.filled(players(2)), Slot.filled(players(3)))
  games += new Game(3, Slot.filled(players(4)), Slot.filled(players(5)))
  games += new Game(4, Slot.filled(players(6)), Slot.filled(players(7)))
  games += new Game(5, Slot.filled(players(8)), Slot.filled(players(9)))
  games += new Game(6, Slot.filled(players(10)), Slot.filled(players(11)))
  games += new Game(7, Slot.filled(players(12)), Slot.filled(players(13)))
  games += new Game(8, Slot.filled(players(14)), Slot.filled(players(15)))

  games += new Game(9, Slot.loserOf(games(1)), Slot.loserOf(games(2)))
  games += new Game(10, Slot.loserOf(games(3)), Slot.loserOf(games(4)))
  games += new Game(11, Slot.loserOf(games(5)), Slot.loserOf(games(6)))
  games += new Game(12, Slot.loserOf(games(7)), Slot.loserOf(games(8)))

  games += new Game(13, Slot.winnerOf(games(1)), Slot.winnerOf(games(2)))
  games += new Game(14, Slot.winnerOf(games(3)), Slot.winnerOf(games(4)))
  games += new Game(15, Slot.winnerOf(games(5)), Slot.winnerOf(games(6)))
  games += new Game(16, Slot.winnerOf(games(7)), Slot.winnerOf(games(8)))

  games += new Game(17, Slot.loserOf(games(13)), Slot.winnerOf(games(9)))
  games += new Game(18, Slot.loserOf(games(14)), Slot.winnerOf(games(10)))
  games += new Game(19, Slot.loserOf(games(15)), Slot.winnerOf(games(11)))
  games += new Game(20, Slot.loserOf(games(16)), Slot.winnerOf(games(12)))

  games += new Game(21, Slot.winnerOf(games(13)), Slot.winnerOf(games(14)))
  games += new Game(22, Slot.winnerOf(games(15)), Slot.winnerOf(games(16)))

  games += new Game(23, Slot.winnerOf(games(17)), Slot.winnerOf(games(18)))
  games += new Game(24, Slot.winnerOf(games(19)), Slot.winnerOf(games(20)))

  games += new Game(25, Slot.winnerOf(games(21)), Slot.winnerOf(games(22)))

  games += new Game(26, Slot.winnerOf(games(23)), Slot.loserOf(games(21)))
  games += new Game(27, Slot.winnerOf(games(24)), Slot.loserOf(games(22)))

  games += new Game(28, Slot.winnerOf(games(26)), Slot.winnerOf(games(27)))
  games += new Game(29, Slot.winnerOf(games(28)), Slot.loserOf(games(25)))
  games += new Game(30, Slot.winnerOf(games(25)), Slot.winnerOf(games(29)))


  val winnerSet = Set(1, 2, 3, 4, 7, 8, 13, 14, 15, 16, 21, 22, 25, 30).map(games.apply)
  val losersSet = Set(9, 10, 11, 12, 17, 18, 19, 20, 23, 24, 26, 27, 28, 29)

  override lazy val finalGame = games(30)

  override val tournamentType: String = "sixteen-player-double-elimination"

}


