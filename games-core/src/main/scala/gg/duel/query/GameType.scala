package gg.duel.query

import gg.duel.SimpleGame

/**
 * Created on 30/10/2015.
 */
sealed trait GameType extends (SimpleGame => Boolean) {
  override def apply(simpleGame: SimpleGame): Boolean =
    this match {
      case All => true
      case Ctf => simpleGame.gameType == "ctf"
      case Duel => simpleGame.gameType == "duel"
    }
}

case object Ctf extends GameType

case object All extends GameType

case object Duel extends GameType

object GameType {
  def unapply(string: String): Option[GameType] = PartialFunction.condOpt(string) {
    case "ctf" => Ctf
    case "all" => All
    case "duel" => Duel
  }
}