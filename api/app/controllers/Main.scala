package controllers

import java.time.ZonedDateTime
import javax.inject._
import gg.duel.uservice.clan.{RegisterClan, Clan}
import gg.duel.uservice.clanplayer.ClanPlayerSystem
import gg.duel.uservice.player.{RegisterPlayer, Player}
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext

/**
 * Created on 13/08/2015.
 */
@Singleton
class Main @Inject()()(implicit executionContext: ExecutionContext) extends Controller {

  var ha: ClanPlayerSystem = ClanPlayerSystem.empty.withPlayer(Player.example).withClan(Clan.example)

  def listPlayers = Action {
    val hu = ha
    import hu.playerEnrich
    Ok(Json.toJson(ha.players.mapValues(_.withClans)))
  }

  def getPlayer(id: String) = Action {
    val hu = ha
    import hu.playerEnrich
    ha.players.get(id) match {
      case Some(player) => Ok(Json.toJson(player.withClans))
      case None => NotFound
    }
  }

  def listClans = Action {
    val hu = ha
    import hu.clanEnrich
    Ok(Json.toJson(ha.clans.mapValues(_.withPlayers)))
  }

  def getClan(id: String) = Action {
    val hh = ha
    import hh.clanEnrich
    ha.clans.get(id) match {
      case Some(clan) => Ok(Json.toJson(clan.withPlayers))
      case None => NotFound
    }
  }

  def registerClan = Action.apply(BodyParsers.parse.json[RegisterClan]) { request =>
    val hh = ha
    import hh.clanEnrich
    ha.registerClanW(request.body, ZonedDateTime.now()) match {
      case Right((sys, clan)) =>
        ha = sys
        Ok(Json.toJson(clan.withPlayers))
      case Left(reason) =>
        BadRequest(reason)
    }
  }

  def registerPlayer = Action.apply(BodyParsers.parse.json[RegisterPlayer]) { request =>
    val hh = ha
    import hh.playerEnrich
    ha.registerPlayerW(request.body, ZonedDateTime.now()) match {
      case Right((sys, player)) =>
        ha = sys
        Ok(Json.toJson(player.withClans))
      case Left(reason) =>
        BadRequest(reason)
    }
  }

}


