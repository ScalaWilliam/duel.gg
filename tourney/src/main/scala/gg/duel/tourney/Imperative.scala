package gg.duel.tourney

object Imperative extends App {

  abstract class Game(var winner: Option[String])
  class LeafGame(name: String) extends Game(Option(name)) {
    override def toString = s"""LeafGame(name = $name)"""
  }
  class PlayedGame(left: Game, right: Game) extends Game(None) {
    override def toString = s"""PlayedGame(winner = $winner, leftName = ${left.winner}, rightName = ${right.winner})"""
  }

  // we want to reduce games by powers-of-two, that's what we want to do!

  // return: (latest iteration, everything combined)
  def reduce(games: List[Game], accumulation: List[Game]): (Game, List[Game]) = {
    if ( games.size == 1 ) {
      (games.head, accumulation)
    } else {
      val (fullTwos, notTwos) = games.splitAt(Math.pow(2, Math.floor(Math.log(games.size) / Math.log(2))).toInt)
      val halvedTwos = fullTwos.sliding(2, 2).map { case List(a, b) => new PlayedGame(a, b)}.toList
      reduce(halvedTwos ++ notTwos, accumulation ++ halvedTwos)
    }
  }

  val yeaaah = reduce(accumulation = Nil, games = List(
    new LeafGame("John"), new LeafGame("Penny"),
    new LeafGame("Peter"), new LeafGame("Smith"), new LeafGame("LolWut")
  ))
  yeaaah._2(0).winner = Option("John")
  yeaaah._2(1).winner = Option("Smith")
  println(yeaaah._1)
  println("---")
  yeaaah._2 foreach println
//  println(yeaaah._2)
//  println(yeaaah._2.size)

}