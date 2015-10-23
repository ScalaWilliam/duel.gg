package gg.duel.uservice.player

import java.time.ZonedDateTime

/**
 * Created on 26/08/2015.
 */
case class CurrentNickname(nickname: String, from: ZonedDateTime) {
  def toPrevious(to: ZonedDateTime) = PreviousNickname(
    nickname = nickname,
    from = from,
    to = to
  )
}
