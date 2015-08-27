package gg.duel.uservice.clanplayer

import java.time.ZonedDateTime

import gg.duel.uservice.clan.{Clan, RegisterClan, SetPatterns}
import gg.duel.uservice.player.{Player, RegisterPlayer, SetNickname}

/**
 * Created on 27/08/2015.
 */
object ClanPlayerSystem {
  def empty = ClanPlayerSystem(clans = Map.empty, players = Map.empty)
}

case class ClanPlayerSystem(clans: Map[String, Clan], players: Map[String, Player]) {

  def withClan(clan: Clan) = copy(clans = clans.updated(clan.id, clan))

  def withPlayer(player: Player) = copy(players = players.updated(player.id, player))

  implicit class playerEnrich(player: Player) {
    def setNickname(sn: SetNickname, currentTime: ZonedDateTime): Either[String, (ClanPlayerSystem, Player)] = {
      players.collectFirst {
        case (id, otherPlayer) if id != player.id && otherPlayer.nicknames.contains(sn.nickname) =>
          otherPlayer
      } match {
        case Some(conflictingPlayer) =>
          Left(s"Other player is using nickname ${sn.nickname}: ${conflictingPlayer.id}")
        case None =>
          val newPlayer = player.withNewNickname(sn.nickname, currentTime)
          val newSystem = copy(players = players.updated(player.id, newPlayer))
          Right(newSystem -> newPlayer)
      }
    }

    def findClan: Option[Clan] = {
      clans.collectFirst{
        case (id, clan)
        if clan.currentPatterns.matchNickname(player.nickname.nickname)
        => clan
      }
    }

    def withClans = PlayerAndClan(player = player, clan = findClan.map(_.id))
  }

  implicit class clanEnrich(clan: Clan) {
    def withPlayers = ClanAndClanPlayers(
      clan = clan,
      clanPlayers = clanPlayers
    )
    def clanPlayers = ClanPlayers(
      players = {
        players.collect{ case (id, player)
        if clan.currentPatterns.matchNickname(player.nickname.nickname) =>
          id -> ClanPlayer(
            id = id, nickname = player.nickname.nickname, countryCode = player.countryCode
          )
        }
      }
    )
    def setPatterns(sp: SetPatterns, currentTime: ZonedDateTime): Either[String, (ClanPlayerSystem, Clan)] = {
      val newClan = clan.withPatterns(sp.patterns, currentTime)
      newClan.currentPatterns.checkInvalidity match {
        case Some(invalidity) => Left(invalidity)
        case None =>
          val newSystem = copy(clans = clans.updated(clan.id, newClan))
          Right(newSystem -> newClan)
      }
    }
  }

  def registerClanW(registerClan: RegisterClan, currentTime: ZonedDateTime): Either[String, (ClanPlayerSystem, Clan)] = {
    clans.get(registerClan.id) match {
      case Some(existingClan) if existingClan.isDerivedFrom(registerClan) =>
        Right((this, existingClan))
      case Some(existingClan) =>
        Left(s"Existing clan conflicts with registration: ${registerClan.id}")
      case None =>
        val newClan = registerClan.toClan(currentTime)
        newClan.currentPatterns.checkInvalidity match {
          case Some(invalidity) => Left(invalidity)
          case None =>
            val newSystem = copy(clans = clans.updated(newClan.id, newClan))
            Right(newSystem -> newClan)
        }
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
