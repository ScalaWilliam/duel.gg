package models

import java.time.ZonedDateTime

/**
 * Created on 14/08/2015.
 */
case class RegisterUser(id: String, google: String, nickname: SetNickname) {
  def toFull(dateTime: ZonedDateTime) = FullUser(
    id = id,
    google = google,
    nickname = nickname.toCurrent(dateTime),
    previousNicknames = List.empty
  )
}
