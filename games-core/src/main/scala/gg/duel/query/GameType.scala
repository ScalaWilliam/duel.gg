package gg.duel.query

import gg.duel.SimpleGame

/**
 * Created on 30/10/2015.
 */
sealed trait GameType extends (SimpleGame => Boolean) {
  def stringValue: String
  override def apply(simpleGame: SimpleGame): Boolean =
    this match {
      case All => true
      case Ctf => simpleGame.gameType == "ctf"
      case Duel => simpleGame.gameType == "duel"
    }
}

case object Ctf extends GameType {
  override val stringValue: String = "ctf"
}

case object All extends GameType {
  override val stringValue: String = "all"
}

case object Duel extends GameType {
  override val stringValue: String = "duel"
}

object GameType {
  def unapply(string: String): Option[GameType] = PartialFunction.condOpt(string) {
    case Ctf.stringValue => Ctf
    case All.stringValue => All
    case Duel.stringValue => Duel
  }
}