package gg.duel.tourney.ripped

import java.util.concurrent.atomic.AtomicInteger

object Gamel {
  val gameCount = new AtomicInteger(0)
  object Result extends Enumeration {
    val P1_WON = Value
    val P2_WON = Value
    val UNDEF = Value
  }
}

class Gamel(val myId: Int, val p1prev: Option[Gamel], val p2prev: Option[Gamel], var result: Gamel.Result.Value) {
  override def equals(other: Any) = myId == other.asInstanceOf[Gamel].myId
  override def hashCode = myId
  def this(p1prev: Option[Gamel], p2prev: Option[Gamel]) =
    this(Gamel.gameCount.incrementAndGet(), p1prev, p2prev, Gamel.Result.UNDEF)
  def this(p1prev: Gamel, p2prev: Gamel) =
    this(Gamel.gameCount.incrementAndGet(), Option(p1prev), Option(p2prev), Gamel.Result.UNDEF)
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
