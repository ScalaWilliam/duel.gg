package gg.duel.query

import java.time.ZonedDateTime

/**
 * Created on 04/10/2015.
 */

sealed trait TimingCondition

case object Recent extends TimingCondition

case object First extends TimingCondition

case class Until(zonedDateTime: ZonedDateTime) extends TimingCondition

case class To(zonedDateTime: ZonedDateTime) extends TimingCondition

case class From(zonedDateTime: ZonedDateTime) extends TimingCondition

case class After(zonedDateTime: ZonedDateTime) extends TimingCondition
