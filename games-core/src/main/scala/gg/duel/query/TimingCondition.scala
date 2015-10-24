package gg.duel.query

/**
 * Created on 04/10/2015.
 */

sealed trait TimingCondition {
  def onVector[T](vector: Vector[T], t: T => String, limit: Int): Vector[T] = {
    this match {
      case Recent => vector.sortBy(t).takeRight(25)
      case First => vector.sortBy(t).take(25)
      case Until(time) => vector.sortBy(t).takeWhile(x => t(x) <= time).takeRight(limit)
      case To(time) => vector.sortBy(t).takeWhile(x => t(x) < time).takeRight(limit)
      case From(time) => vector.sortBy(t).dropWhile(x => t(x) < time).take(limit)
      case After(time) => vector.sortBy(t).dropWhile(x => t(x) <= time).take(limit)
    }
  }
}

case object Recent extends TimingCondition

case object First extends TimingCondition

case class Until(time: String) extends TimingCondition

case class To(time: String) extends TimingCondition

case class From(time: String) extends TimingCondition

case class After(time: String) extends TimingCondition
