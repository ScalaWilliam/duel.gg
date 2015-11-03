package gg.duel.query

import gg.duel.query.LookupDirection.{Before, After}

import scala.language.higherKinds

sealed trait TimingCondition {
  def stringValue: String
  def isRecent = this == Recent
  def isFirst = this == First
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

sealed trait LookupDirection {
  def stringValue: String
  def httpValue: String
  def isBefore = this == Before
  def isAfter = this == After
  def opposite = if ( isAfter ) Before else After
}
object LookupDirection {
  def unapply(string: String): Option[LookupDirection] = PartialFunction.condOpt(string) {
    case After.stringValue => After
    case Before.stringValue => Before
  }
  case object After extends LookupDirection {
    override val stringValue = "after"
    override val httpValue = "next"
  }
  case object Before extends LookupDirection {
    override val stringValue = "before"
    override val httpValue = "previous"
  }
}