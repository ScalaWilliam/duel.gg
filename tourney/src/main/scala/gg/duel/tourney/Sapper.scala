package gg.duel.tourney

import gg.duel.tourney.scala.Roundel


object Sapper extends App {

  def calculateRounds(lineup: List[String]) = {
    val firstRound = Roundel(lineup)
    firstRound +: Iterator.iterate(firstRound.getNextRound.orNull)(_.getNextRound.orNull).takeWhile(_ != null).toList
  }

  val rounds = calculateRounds(List("John", "Poop", "Exy", "Matt", "Smith"))
  rounds foreach println
  
  val allGames = rounds.map(r =>r.winnerGames++r.loserGames).flatten.map(g=>List(g)++g.p1prev.toList ++g.p2prev.toList).flatten.toSet.toList
  allGames.sortBy(_.myId) foreach println

}