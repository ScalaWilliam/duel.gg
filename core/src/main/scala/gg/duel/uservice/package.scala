package gg.duel

import java.time.ZonedDateTime

/**
 * Created on 26/08/2015.
 */
package object uservice {

  case class CurrentNickname(nickname: String, from: ZonedDateTime) {
    def toPrevious(to: ZonedDateTime) = PreviousNicknameCountry(
      nickname = nickname,
      from = from,
      to = to
    )
  }

  case class PreviousNicknameCountry(nickname: String, from: ZonedDateTime, to: ZonedDateTime)

  case class Player(id: String, countryCode: String, google: String, nickname: CurrentNickname, previousNicknames: List[PreviousNicknameCountry]) {
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

  case class Pattern(value: String)

  case class CurrentPatterns(patterns: List[Pattern], from: ZonedDateTime) {
    def toPrevious(currentTime: ZonedDateTime): PreviousPatterns =
      PreviousPatterns(
        patterns = patterns,
        from = from,
        to = currentTime
      )
  }

  case class PreviousPatterns(patterns: List[Pattern], from: ZonedDateTime, to: ZonedDateTime)

  case class Clan(id: String, patterns: CurrentPatterns, previousPatterns: List[PreviousPatterns]) {
    def withPatterns(newPatterns: List[Pattern], currentTime: ZonedDateTime): Clan =
      if (patterns.patterns.toSet == newPatterns.toSet) this
      else copy(
        patterns = CurrentPatterns(
          patterns = newPatterns,
          from = currentTime
        ),
        previousPatterns = previousPatterns :+ patterns.toPrevious(currentTime = currentTime)
      )
    def isDerivedFrom(registerClan: RegisterClan): Boolean = {
      id == registerClan.id && patterns.patterns.map(_.value).toSet == registerClan.patterns.toSet
    }
  }

  case class RegisterClan(id: String, patterns: List[String], startTime: Option[ZonedDateTime]) {
    def toClan(currentTime: ZonedDateTime) =
      Clan(
        id = id,
        patterns = CurrentPatterns(
          patterns = patterns.map(Pattern),
          from = startTime.getOrElse(currentTime)
        ),
        previousPatterns = List.empty
      )
  }

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

  case class ClanPlayerSystem(clans: Map[String, Clan], players: Map[String, Player]) {

    def registerClanW(registerClan: RegisterClan, currentTime: ZonedDateTime): Either[String, (ClanPlayerSystem, Clan)] = {
      clans.get(registerClan.id) match {
        case Some(existingClan) if existingClan.isDerivedFrom(registerClan) =>
          Right((this, existingClan))
        case Some(existingClan) =>
          Left(s"Existing clan conflicts with registration: ${registerClan.id}")
        case None =>
          val newClan = registerClan.toClan(currentTime)
          val newSystem = copy(clans = clans.updated(newClan.id, newClan))
          Right(newSystem -> newClan)
      }
    }

    def registerPlayerW(registerPlayer: RegisterPlayer, currentTime: ZonedDateTime): Either[String, (ClanPlayerSystem, Player)] = {
      players.get(registerPlayer.id) match {
        case Some(existingPlayer) if existingPlayer.isDerivedFrom(registerPlayer) =>
          Right(this -> existingPlayer)
        case Some(existingPlayer) =>
          Left("Conflict: user already registered but with different credentials")
        case None =>
          // check if anyone has already used this nickname
          players.collectFirst {
            case (id, player) if player.nicknames.contains(registerPlayer.nickname) => id
          } match {
            case Some(conflictingUserId) =>
              Left(s"Conflict: another user has already used your nickname, his ID is: $conflictingUserId")
            case None =>
              val newPlayer = registerPlayer.toPlayer(currentTime)
              val newSystem = copy(players = players + (registerPlayer.id -> newPlayer))
              Right(newSystem -> newPlayer)
          }
      }
    }

  }

}
