package controllers

import javax.inject._

import models.lookups.UserLookup
import models._
import play.api.libs.EventSource
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import play.api.mvc._

import scala.async.Async
import scala.concurrent.ExecutionContext



/**
 * Created on 13/08/2015.
 */
class Main @Inject()(usersRepository: UsersRepository)(implicit executionContext: ExecutionContext) extends Controller {

  def listUsers = Action {
    Ok(Json.toJson(usersRepository.users.users.map{case (UserId(k), v) => k -> v}))
  }

  def lookupUser(userLookup: UserLookup) = Action {
    usersRepository.users match {
      case userLookup(user) => Ok(Json.toJson(user))
      case _ => NotFound
    }
  }

  def userById(id: UserId) = Action {
    usersRepository.users.users.get(id) match {
      case Some(user) =>
        Ok(Json.toJson(user))
      case _ =>
        NotFound
    }
  }

  def setNicknameForUser(id: UserId) = Action.async(BodyParsers.parse.json[SetNickname]) { request =>
    Async.async {
      Async.await(usersRepository.setNickname(id, request.body)) match {
        case Right(fullUser) => Ok(Json.toJson(fullUser))
        case Left(reason) => Conflict(reason)
      }
    }
  }

  def uud = usersRepository.userUpdatesEnum.map(user => Json.stringify(Json.toJson(user)))

  def userUpdatesWs = WebSocket.using[String]{ rh =>
    val in = Iteratee.ignore[String]
    (in, uud)
  }

  def userUpdatesEs = Action {
    Ok.feed(uud &> EventSource()).as("text/event-stream")
  }

  def registerUser = Action.async(BodyParsers.parse.json[RegisterUser]) { request =>
    Async.async {
      Async.await(usersRepository.registerUser(request.body)) match {
        case Right(user) => Ok(Json.toJson(user))
        case Left(reason) => Conflict(reason)
      }
    }
  }

}


