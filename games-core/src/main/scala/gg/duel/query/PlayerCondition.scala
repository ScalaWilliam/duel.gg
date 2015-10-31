package gg.duel.query

/**
 * Created on 04/10/2015.
 */
sealed trait PlayerConditionOperator
case object And extends PlayerConditionOperator
case object Or extends PlayerConditionOperator
object PlayerConditionOperator {
  def unapply(string: String): Option[PlayerConditionOperator] = PartialFunction.condOpt(string) {
    case "or" => Or
    case "and" => And
  }
}

case class PlayerCondition(player: Set[String], user: Set[String], clan: Set[String], playerConditionOperator: PlayerConditionOperator)

case class TagFilter(tags: Set[String])
