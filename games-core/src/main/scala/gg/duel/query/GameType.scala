package gg.duel.query

/**
 * Created on 30/10/2015.
 */
sealed trait GameType

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