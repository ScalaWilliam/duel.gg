package gg.duel.uservice.player

import java.time.ZonedDateTime

/**
* Created on 27/08/2015.
*/
case class RegisterPlayer(id: String, google: String, nickname: String, countryCode: String, startTime: Option[ZonedDateTime]) {
  def toPlayer(currentTime: ZonedDateTime) = Player(
    id = id, google = google,
    previousNicknames = List.empty,
    countryCode = countryCode,
    nickname = CurrentNickname(
      nickname = nickname,
      from = startTime.getOrElse(currentTime)
    )
  )
}
