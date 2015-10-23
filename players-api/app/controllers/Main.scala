package controllers

import javax.inject._

import gg.duel.uservice.clan.{RegisterClan, SetPatterns}
import gg.duel.uservice.player.{RegisterPlayer, SetNickname}
import play.api.libs.json.Json
import play.api.mvc._
import services.{AuthenticationService, PlayerClanManager}

import scala.async.Async
import scala.concurrent.{Future, ExecutionContext}

/**
 * Created on 13/08/2015.
 */
@Singleton
class Main @Inject()(playerClanManager: PlayerClanManager,
                     authenticationService: AuthenticationService
                      )(implicit executionContext: ExecutionContext) extends Controller {

  def WriteCheckAction = new ActionBuilder[Request] {
    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      request.getQueryString("api-key") match {
        case Some(apiKey) if authenticationService.authenticates(apiKey) => block(request)
        case _ => Future.successful(Unauthorized("A valid API key is required."))
      }
    }
  }

  def cps = playerClanManager.cps

  def listPlayers = Action {
    Ok(Json.toJson(cps.getRichPlayers))
  }

  def getPlayer(id: String) = Action {
    cps.getRichPlayer(id) match {
      case Some(player) => Ok(Json.toJson(player))
      case None => NotFound
    }
  }

  def listClans = Action {
    Ok(Json.toJson(cps.getRichClans))
  }

  def getClan(id: String) = Action {
    cps.getRichClan(id) match {
      case Some(clan) => Ok(Json.toJson(clan))
      case None => NotFound
    }
  }

  def registerPlayer = WriteCheckAction.async(BodyParsers.parse.json[RegisterPlayer]) { request =>
    Async.async {
      Async.await(playerClanManager.registerPlayer(request.body, now())) match {
        case Right(playerAndClan) =>
          Ok(Json.toJson(playerAndClan))
        case Left(reason) =>
          BadRequest(reason)
      }
    }
  }

  def registerClan = WriteCheckAction.async(BodyParsers.parse.json[RegisterClan]) { request =>
    Async.async {
      Async.await(playerClanManager.registerClan(request.body, now())) match {
        case Right(clanWithPlayers) =>
          Ok(Json.toJson(clanWithPlayers))
        case Left(reason) =>
          BadRequest(reason)
      }
    }
  }

  def setPatterns(clanId: String) = WriteCheckAction.async(BodyParsers.parse.json[SetPatterns]) { request =>
    Async.async {
      Async.await(playerClanManager.setPatterns(clanId, request.body, now())) match {
        case None => NotFound("Clan not found")
        case Some(Left(failure)) => BadRequest(failure)
        case Some(Right(clanAndClanPlayers)) => Ok(Json.toJson(clanAndClanPlayers))
      }
    }
  }

  def setNickname(playerId: String) = WriteCheckAction.async(BodyParsers.parse.json[SetNickname]) { request =>
    Async.async {
      Async.await(playerClanManager.setNickname(playerId, request.body, now())) match {
        case None => NotFound("Player not found")
        case Some(Left(failure)) => BadRequest(failure)
        case Some(Right(playerWithClan)) => Ok(Json.toJson(playerWithClan))
      }
    }
  }

}


