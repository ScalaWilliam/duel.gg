package gg.duel.query

import scala.language.higherKinds

sealed trait TimingCondition {
  def stringValue: String
}
object TimingCondition {
  def unapply(string: String): Option[TimingCondition] = PartialFunction.condOpt(string) {
    case "recent" => Recent
    case "first" => First
  }
}

case object Recent extends TimingCondition {
  override def stringValue: String = "recent"
}

case object First extends TimingCondition {
  override def stringValue: String = "first"
}
