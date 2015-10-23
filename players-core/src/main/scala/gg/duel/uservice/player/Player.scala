package gg.duel.uservice.player

import java.time.ZonedDateTime

/**
* Created on 27/08/2015.
*/
object Player {
  def example = Player(
    id = "drakas",
    google = "drakas@woop.us",
    countryCode = "GB",
    nickname = CurrentNickname(
      nickname = "w00p|Drakas",
      from = ZonedDateTime.now().minusYears(1).withNano(0)
    ),
    previousNicknames = List.empty
  )
}

case class Player(id: String, countryCode: String, google: String, nickname: CurrentNickname, previousNicknames: List[PreviousNickname]) {
  def withNewNickname(newNickname: String, currentTime: ZonedDateTime): Player =
    if (nickname.nickname == newNickname) this
    else copy(
      nickname = CurrentNickname(
        nickname = newNickname,
        from = currentTime
      ),
      previousNicknames = previousNicknames :+ nickname.toPrevious(currentTime)
    )
  def isDerivedFrom(registerPlayer: RegisterPlayer): Boolean = {
    id == registerPlayer.id &&
      countryCode == registerPlayer.countryCode &&
      google == registerPlayer.google &&
      nickname.nickname == registerPlayer.nickname
  }
  def nicknames: Set[String] = Set(nickname.nickname) ++ previousNicknames.map(_.nickname).toSet
}