package controllers

import java.util.UUID

import controllers.Duelgg._
import play.api.mvc._
import plugins._
import scala.concurrent.Future

object Users extends Controller {


  case class ProfileForm(
  sauerbratenNickname: String,
  permanentNickname: String,
  permanentUrl: String,
  shareEmail: Boolean
  )

  import play.api.data._
  import play.api.data.Forms._

  val profileFormMapping = mapping(
    "sauerbratenNickname" -> text,
    "permanentNickname" -> text,
    "permanentUrl" -> text,
    "shareEmail" -> boolean
  )(ProfileForm.apply)(ProfileForm.unapply)

  val profileForm: Form[ProfileForm] =
    Form(profileFormMapping)

  import scala.concurrent.ExecutionContext.Implicits.global

  // Only assign a session ID at this point. And give it a session token as well
  def login = Action.async {
    implicit request =>
      val sessionId = request.cookies.get(SESSION_ID).map(_.value).getOrElse(UUID.randomUUID().toString)
      val sessionCookie = Cookie(SESSION_ID, sessionId, maxAge = Option(200000))
      val newTokenValue = UUID.randomUUID().toString
      UserManagement.userManagement.sessionTokens.put(sessionId, newTokenValue)
      Future { TemporaryRedirect(UserManagement.userManagement.authUrl(newTokenValue)).withCookies(sessionCookie) }
  }

  def createProfile = stated { implicit request => implicit suzzy =>
    suzzy.profile match {
      case Some(_) => Future {
        Ok("You already have a profile, naughty!")
      }
      case _ => Future {
        Ok(views.html.second.createProfile(profileForm))
      }
    }
  }

  def logout = Action {
    implicit request =>
      request.cookies.get(SESSION_ID).map(_.value).foreach(UserManagement.userManagement.sessionEmails.remove)
      TemporaryRedirect(controllers.routes.Duelgg.index().absoluteURL())
  }


  def loggy = Action.async {
    implicit request =>
      Future{Ok(views.html.loggy("---"))}
  }


  def oauth2callback = Action.async {
    implicit request =>
      val code = request.queryString("code").head
      val state = request.queryString("state").head
      val sessionId = request.cookies(SESSION_ID).value
      val expectedState = UserManagement.userManagement.sessionTokens.get(sessionId)
      //      UserManagement.userManagement.sessionEmails.remove(sessionId)
      UserManagement.userManagement.sessionTokens.remove(sessionId)
      if ( state != expectedState ) {
        throw new RuntimeException(s"Expected $expectedState, got $state")
      }

      for {
        user <- UserManagement.userManagement.acceptOAuth(code)
      } yield {
        UserManagement.userManagement.sessionEmails.put(sessionId, user.email)
        TemporaryRedirect(controllers.routes.Duelgg.index().absoluteURL())
      }
  }



}
