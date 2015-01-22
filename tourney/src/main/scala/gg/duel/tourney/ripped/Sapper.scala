package gg.duel.tourney.ripped

import gg.duel.tourney.ripped.Gamel.Result


object Sapper extends App {

  case class CalculatedRounds(rounds: List[Roundel], games: List[Gamel], finalGame: Gamel)

  def calculateRounds(lineup: List[String]) = {
    val firstRound = Roundel(lineup)
    val rounds = firstRound +: Iterator.iterate(firstRound.getNextRound.orNull)(_.getNextRound.orNull).takeWhile(_ != null).toList
    rounds
  }

  val rounds = calculateRounds(List("A", "B", "C", "D"))

  val games = rounds.flatMap(r => r.winnerGames ++ r.loserGames).toList
  val winnerGames = rounds.flatMap(_.winnerGames).toList
  val loserGames = rounds.flatMap(_.loserGames).toList

  games foreach println
  println("--")
  winnerGames foreach println
  println("--")
  loserGames foreach println
}