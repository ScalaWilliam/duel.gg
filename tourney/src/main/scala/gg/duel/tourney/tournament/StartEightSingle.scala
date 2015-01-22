package gg.duel.tourney.tournament

/**
 * Created by William on 22/01/2015.
 */
class StartEightSingle(val players: List[String], thirdPlaceMatch: Boolean = false) extends Tournament {
  override val tournamentType = "single-elimination-eight-players"
  games += new Game(1, Slot.filled(players(0)), Slot.filled(players(7)))
  games += new Game(2, Slot.filled(players(1)), Slot.filled(players(6)))
  games += new Game(3, Slot.filled(players(2)), Slot.filled(players(5)))
  games += new Game(4, Slot.filled(players(3)), Slot.filled(players(4)))
  games += new Game(5, Slot.winnerOf(games(1)), Slot.winnerOf(games(2)))
  games += new Game(6, Slot.winnerOf(games(3)), Slot.winnerOf(games(4)))
  games += new Game(7, Slot.winnerOf(games(5)), Slot.winnerOf(games(6)))
  if ( thirdPlaceMatch ) {
    games += new Game(8, Slot.loserOf(games(5)), Slot.loserOf(games(6)))
  }
  def thirdPlace = if ( thirdPlaceMatch ) Option(games(8)) else None
  override val finalGame = games(7)
}
