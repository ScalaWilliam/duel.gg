package gg.duel.tourney.scala

class LeafGamel(val playerName: Option[String], val winningBracketGame: Option[Gamel], result: Gamel.Result.Value) extends Gamel(None, None) {
  def this(player: String) = this(Option(player), None, Gamel.Result.P1_WON)
  def this(game: Gamel) = this(None, Option(game), Gamel.Result.P1_WON)
  override def getWinner = playerName orElse winningBracketGame.flatMap(_.getLoser)
  override def getLoser = Option.empty[String]
  override def toString = s"LeafGamel(myId = $myId, winner = $getWinner, winningBracketGame = ${winningBracketGame.map(_.myId)})"
}
