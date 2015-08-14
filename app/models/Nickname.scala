package models

import java.time.ZonedDateTime

/**
 * Created on 14/08/2015.
 */

sealed trait Nickname {
  def nickname: String

  def countryCode: String

  def from: ZonedDateTime

  def toO: Option[ZonedDateTime]
}
case class PreviousNickname(nickname: String, countryCode: String, from: ZonedDateTime, to: ZonedDateTime) extends Nickname {
  override def toO: Option[ZonedDateTime] = Option(to)
}
case class CurrentNickname(nickname: String, countryCode: String, from: ZonedDateTime) extends Nickname {
  override def toO: Option[ZonedDateTime] = Option.empty
  def toPrevious(toTime: ZonedDateTime) = PreviousNickname(
    nickname = nickname,
    countryCode = countryCode,
    from = from,
    to = toTime
  )
}
