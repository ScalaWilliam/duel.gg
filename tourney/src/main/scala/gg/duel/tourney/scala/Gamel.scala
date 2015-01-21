package gg.duel.tourney.scala

object Gamel {
  var gameCount = 0
  object Result extends Enumeration {
    val P1_WON = Value
    val P2_WON = Value
    val UNDEF = Value
  }
}
class Gamel(val myId: Int, val p1prev: Option[Gamel], val p2prev: Option[Gamel], var result: Gamel.Result.Value) {
  def this(p1prev: Option[Gamel], p2prev: Option[Gamel]) =
    this({Gamel.gameCount = Gamel.gameCount + 1; Gamel.gameCount}, p1prev, p2prev, Gamel.Result.UNDEF)
  def this(p1prev: Gamel, p2prev: Gamel) =
    this({Gamel.gameCount = Gamel.gameCount + 1; Gamel.gameCount}, Option(p1prev), Option(p2prev), Gamel.Result.UNDEF)
  def getWinner: Option[String] = {
    p2prev match {
      case None => p1prev.flatMap(_.getWinner)
      case Some(_) =>
        result match {
          case Gamel.Result.P1_WON => p1prev.flatMap(_.getWinner)
          case Gamel.Result.P2_WON => p2prev.flatMap(_.getWinner)
          case _ => None
        }
    }
  }
  def getLoser: Option[String] = {
    result match {
      case Gamel.Result.P1_WON => p2prev.flatMap(_.getWinner)
      case Gamel.Result.P2_WON => p1prev.flatMap(_.getWinner)
      case _ => None
    }
  }
  override def toString = s"""Gamel(myId = $myId, game1 = ${p1prev.map(_.myId)}, game2 = ${p2prev.map(_.myId)}, winner = $getWinner, loser = $getLoser)"""
}
