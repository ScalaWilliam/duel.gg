import java.time.ZonedDateTime

import gg.duel.uservice.clan.{CurrentPatterns, PreviousPatterns, RegisterClan, SetPatterns}
import gg.duel.uservice.clanplayer.{ClanPlayer, ClanAndClanPlayers, PlayerAndClan}
import gg.duel.uservice.player.{CurrentNickname, PreviousNickname, RegisterPlayer, SetNickname}
import modules.AuthenticationService
import play.api.libs.json._
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future

/**
 * Created on 27/08/2015.
 */
package object controllers {


  def WriteCheckAction(implicit authenticationService: AuthenticationService): ActionBuilder[Request] =
    new ActionBuilder[Request] with Results {
      override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
        request.getQueryString("api-key") match {
          case Some(apiKey) if authenticationService.authenticates(apiKey) => block(request)
          case _ => Future.successful(Unauthorized("A valid API key is required."))
        }
      }
    }

  def now(): ZonedDateTime = ZonedDateTime.now()

  implicit val registerPlayerReads = Json.reads[RegisterPlayer]
  implicit val registerClanReads = Json.reads[RegisterClan]

  implicit val previousPatternsWrites: Writes[PreviousPatterns] = Json.writes[PreviousPatterns]
  implicit val currentPatternsWrites = Json.writes[CurrentPatterns]

  implicit val clanWrites = Json.writes[gg.duel.uservice.clan.Clan]

  implicit val previousNicknameWrites = Json.writes[PreviousNickname]
  implicit val currentNicknameWrites = Json.writes[CurrentNickname]
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
