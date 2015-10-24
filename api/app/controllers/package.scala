import java.time.ZonedDateTime

import gg.duel.uservice.clan.{SetPatterns, CurrentPatterns, PreviousPatterns, RegisterClan}
import gg.duel.uservice.clanplayer.{PlayerAndClan, ClanAndClanPlayers}
import gg.duel.uservice.player.{SetNickname, CurrentNickname, PreviousNickname, RegisterPlayer}
import play.api.libs.json._

/**
 * Created on 27/08/2015.
 */
package object controllers {

  def now(): ZonedDateTime = ZonedDateTime.now()

  implicit val registerPlayerReads = Json.reads[RegisterPlayer]
  implicit val registerClanReads = Json.reads[RegisterClan]

  implicit val previousPatternsWrites: Writes[PreviousPatterns] = Json.writes[PreviousPatterns]
  implicit val currentPatternsWrites = Json.writes[CurrentPatterns]

  implicit val clanPlayerWrites = Json.writes[ClanPlayer]
  implicit val clanWrites = Json.writes[gg.duel.uservice.clan.Clan]

  implicit val playerNicknameWrites = Json.writes[PlayerNickname]
  implicit val previousNicknameWrites = Json.writes[PreviousNickname]
  implicit val currentNicknameWrites = Json.writes[CurrentNickname]
  implicit val playerWrites = Json.writes[Player]
  implicit val pplayerWrites = Json.writes[gg.duel.uservice.player.Player]
  implicit val cplayerWrites = Json.writes[gg.duel.uservice.clanplayer.ClanPlayer]

  implicit val setPatternsReads = Json.reads[SetPatterns]
  implicit val setNicknameReads = Json.reads[SetNickname]

  implicit val clanWithPlayersWrites: Writes[ClanAndClanPlayers] = new Writes[ClanAndClanPlayers] {
    override def writes(o: ClanAndClanPlayers): JsValue = {
      Json.toJson(o.clan) match {
        case a: JsObject => a + ("players" -> Json.toJson(o.clanPlayers.players))
        case other => other
      }
    }
  }
  implicit val playerAndClanWrites: Writes[PlayerAndClan] = new Writes[PlayerAndClan] {
    override def writes(o: PlayerAndClan): JsValue = {
      (Json.toJson(o.player), o.clan) match {
        case (a: JsObject, Some(clan)) =>
          a + ("clan" -> JsString(clan))
        case (other, _) => other
      }
    }
  }
}
