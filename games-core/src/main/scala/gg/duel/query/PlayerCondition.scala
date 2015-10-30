package gg.duel.query

/**
 * Created on 04/10/2015.
 */
sealed trait Operand
case object And extends Operand
case object Or extends Operand
object  Operand {
  def unapply(string: String): Option[Operand] = PartialFunction.condOpt(string) {
    case "or" => Or
    case "and" => And
  }
}
case class PlayerCondition(player: Set[String], user: Set[String], clan: Set[String], operand: Operand)
