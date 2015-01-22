package gg.duel.tourney


import org.scalatest.{Inside, Inspectors, Matchers, WordSpec}

class DoubleElimSpec extends WordSpec with Matchers with Inspectors with Inside {
  import gg.duel.tourney.DoubleElimination._
  implicit class addit(x: DoubleEliminationTournament) {
    def print(): Unit = {
      x.games.toList.sortBy(_._1).foreach(println)
    }
  }
  val start = DoubleEliminationTournament.startFour(10, List("John", "Betsy", "Raphael", "BigD"))
  "Double elim logic" must {
    "Winners: winner -> winners, loser -> losers" in {
      val a = start.withWonGame(1, "John", "Betsy")
      a.print()
      println("--")
      val b = a.withWonGame(2, "Raphael", "BigD")
      b.print()
      println("--")
      val c = b.withWonGame(3, "Raphael", "John")
      c.print()
      println("--")
      val d = c.withWonGame(4, "BigD", "Betsy")
      d.print()
      println("--")
      val e = d.withWonGame(5, "BigD", "John")
      e.print()
      println("--")
      // works fine
    }
    "Deals with fails correctly" in {
      val a = start.withFailedGame(1, "Boring")
      a.print()
      println("--")
      val b = a.withWonGame(2, "BigD", "Raphael")
      b.print()
      println("--")
    }
  }
}