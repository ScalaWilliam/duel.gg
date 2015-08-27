package gg.duel.uservice.clanplayer

import gg.duel.uservice.clan.Clan

/**
 * Created on 27/08/2015.
 */
case class ClanPlayer(id: String, nickname: String, countryCode: String)
case class ClanPlayers(players: Map[String, ClanPlayer]) {

}

case class ClanAndClanPlayers(clan: Clan, clanPlayers: ClanPlayers)