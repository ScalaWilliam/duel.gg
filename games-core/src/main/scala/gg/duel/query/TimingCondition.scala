package gg.duel.query

import scala.language.higherKinds

sealed trait TimingCondition
object TimingCondition {
  def unapply(string: String): Option[TimingCondition] = PartialFunction.condOpt(string) {
    case "recent" => Recent
    case "first" => First
  }
}

case object Recent extends TimingCondition

case object First extends TimingCondition
