package controllers

import java.util.UUID

import play.api.Play
import play.api.libs.json.{JsArray, Json, JsString, JsValue}
import play.api.libs.ws.WS
import play.api.mvc._
import play.twirl.api.Html
import plugins._
import scala.concurrent.Future
import scala.xml.PCData

object Duelgg extends Controller {

  case class Server(connect: String, alias: Option[String])
  case class ServersListing(servers: List[Server])

  val SESSION_ID = "sessionId"
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

  def logout = Action {
    implicit request =>
      request.cookies.get(SESSION_ID).map(_.value).foreach(UserManagement.userManagement.sessionEmails.remove)
      TemporaryRedirect(controllers.routes.Duelgg.index().absoluteURL())
  }

  def index = Action.async {
    request =>
      UserManagement.userManagement.getSessionState(request).flatMap { implicit suzzy =>
        request.queryString.get("player").toList.flatten.headOption match {
          case Some(playerName) =>
            for {
              xmlData <- DuelsInterface.duelsInterface.getPlayer(playerName)
            } yield xmlData match {
              case Some(data) if data.nonEmpty => Ok(views.html.player(playerName)(Html(s"$data")))
              case _ => NotFound
            }
          case None =>
            for {
              xmlData <- DuelsInterface.duelsInterface.getIndex
            } yield Ok(views.html.index(Html(s"$xmlData")))
        }
      }
  }

  def showPage(id: String) = Action.async {
    request =>
      val resultO = for {
        xmlO <- DuelsInterface.duelsInterface.getDuel(id)
      } yield for {
        xml <- xmlO
        title = (xml \ "title").text
        article = xml \ "article"
      } yield Ok(views.html.duel(title)(Html(s"$article")))

      resultO.map(_.getOrElse(NotFound))
  }

  def streamSocket = {
    import Play.current
    WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
      DuelsStream.duelsStream.createListenerActor(out)
    }
  }
/*
  val jizzin = """
                 |{
                 |access_token: "***REMOVED***",
                 |token_type: "Bearer",
                 |expires_in: 3600,
                 |id_token: "***REMOVED***",
                 |refresh_token: "***REMOVED***"
                 |}""".stripMargin
*/
  def showLeague = Action.async {
    request =>
      import concurrent.duration._
      for {
        rep <- LeagueInterface.leagueInterface.requestData(1.second)
        xmlO = Option(rep.value).map(s => Html(s"$s"))
      } yield Ok(views.html.league(xmlO))
  }

  def loggy = Action.async {
    implicit request =>
      Future{Ok(views.html.loggy("---"))}
  }


  def oauth2callback = Action.async {
    implicit request =>
      import Play.current
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

  def showQuestions = Action.async {
    request =>
      for { servers <- DuelsInterface.duelsInterface.holder.post(<query xmlns="http://basex.org/rest">
        <text><![CDATA[<servers>{/server[@connect and not(@inactive)]}</servers>]]></text>
      </query>)
        serversListing = for {
          server <- servers.xml \ "server"
          connect <- server \ "@connect" map (_.text)
          alias = (server \ "@alias" map (_.text)).headOption
        } yield Server(connect, alias)
      } yield Ok(views.html.questions(ServersListing(serversListing.toList)))
  }

}