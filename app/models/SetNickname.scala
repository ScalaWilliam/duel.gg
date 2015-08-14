package models

import java.time.ZonedDateTime

case class SetNickname(nickname: String, countryCode: String) {
  def toCurrent(fromDateTime: ZonedDateTime) = CurrentNickname(
    nickname = nickname,
    countryCode = countryCode,
    from = fromDateTime
  )
}
