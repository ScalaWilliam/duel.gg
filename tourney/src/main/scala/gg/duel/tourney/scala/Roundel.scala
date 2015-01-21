package gg.duel.tourney.scala

/** Knocked off from package se.aoeu.bracketapplet - BracketApplet.jar
  * http://www.csc.kth.se/~landreas/tournament/
  */

object Roundel {
  def apply(players: List[String]): Roundel =
    Roundel(players.map(n => new LeafGamel(n)), List.empty)

  def apply(finalGame: Gamel): Roundel =
    Roundel(Nil, List(finalGame))

}

case class Roundel(winnerGames: List[Gamel], loserGames: List[Gamel]) {
  def getNextRound: Option[Roundel] = {
    val newLoserGames = {
      val newPrevLGs = collection.mutable.ArrayBuffer[Gamel](loserGames: _*)
      var insPos = 0
      for {
        wg <- winnerGames
        if wg.p2prev.nonEmpty
      } {
        newPrevLGs.insert(insPos, new LeafGamel(wg))
        insPos = Math.min(insPos + 2, newPrevLGs.size)
      }
      newPrevLGs.toList
    }
    if (newLoserGames.size == 1 && winnerGames.isEmpty) {
      None
    } else if (winnerGames.size == 1 && newLoserGames.size == 1) {
      Option(Roundel(new Gamel(Option(winnerGames.head), Option(newLoserGames(0)))))
    } else {
      val newWGs1 = winnerGames match {
        case one :: Nil => List(new Gamel(one, null))
        case one :: two :: three :: rest if winnerGames.size % 2 == 1 =>
          List(new Gamel(one, two), new Gamel(three, null)) ++ {
            for { a :: b :: Nil <- rest.sliding(2,2) } yield new Gamel(a, b)
          }
        case other => for { a::b::Nil <- other.sliding(2,2) } yield new Gamel(a, b)
      }
      val newLGs1 = {
        if ( newLoserGames.size % 2 == 0 ) {
          for {a :: b :: Nil <- newLoserGames.sliding(2, 2).map(_.toList)} yield new Gamel(a, b)
        } else {
          val first = List(new Gamel(newLoserGames(0), null))
          val second = for {a :: b :: Nil <- newLoserGames.drop(1).sliding(2, 2).map(_.toList)} yield new Gamel(a, b)
          first ++ second
        }
      }
      Option(Roundel(newWGs1.toList, newLGs1.toList))
    }
  }
}