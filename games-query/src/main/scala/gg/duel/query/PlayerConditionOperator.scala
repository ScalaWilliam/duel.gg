package gg.duel.query


sealed trait PlayerConditionOperator {
  def stringValue: String

  def isOr = this == PlayerConditionOperator.Or

  def isAnd = this == PlayerConditionOperator.And
}

object PlayerConditionOperator {
  def unapply(string: String): Option[PlayerConditionOperator] = PartialFunction.condOpt(string) {
    case Or.stringValue => Or
    case And.stringValue => And
  }

  case object And extends PlayerConditionOperator {
    override val stringValue: String = "and"
  }

  case object Or extends PlayerConditionOperator {
    override val stringValue: String = "or"
  }

}
