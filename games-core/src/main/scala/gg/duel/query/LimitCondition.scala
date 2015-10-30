package gg.duel.query

/**
 * Created on 30/10/2015.
 */
sealed trait LimitCondition
case object DefaultLimit extends LimitCondition
case class SpecificLimit(limit: Int) extends LimitCondition
