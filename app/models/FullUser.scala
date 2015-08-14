package models

import java.time.ZonedDateTime

/**
 * Created on 14/08/2015.
 */
object FullUser {
  def example = FullUser(
    id = "drakas",
    google = "drakas@woop.us",
    nickname = CurrentNickname(
      nickname = "w00p|Drakas",
      countryCode = "GB",
      from = ZonedDateTime.now().minusYears(1).withNano(0)
    ),
    previousNicknames = List.empty
  )
}



case class FullUser(id: String, google: String, nickname: CurrentNickname, previousNicknames: List[PreviousNickname]) {
  def nicknames: List[Nickname] = nickname +: previousNicknames
  def withNewNickname(setNickname: SetNickname, dateTime: ZonedDateTime): FullUser = {
    if ( nickname.nickname == setNickname.nickname && nickname.countryCode == setNickname.countryCode )
      this
    else
      copy(
        nickname = setNickname.toCurrent(dateTime),
        previousNicknames = previousNicknames :+ nickname.toPrevious(dateTime)
      )
  }
}