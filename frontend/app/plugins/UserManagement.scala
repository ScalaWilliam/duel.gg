package plugins

import controllers.routes
import play.api._
import play.api.libs.json.{JsObject, JsArray, JsString}
import play.api.libs.ws.WS
import play.api.mvc.Request
import plugins.UserManagement.{SessionState, GoogleUser, Profile}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.xml.PCData

import org.scalactic._
/**
 * Have two LFU maps:
 * SessionID -> Token
 * SessionID -> Authentication E-mail
 *
 * We can then evict stuff quite easily
 *
 * @param app
 */

object UserManagement {

  def userManagement: UserManagement = Play.current.plugin[UserManagement]
    .getOrElse(throw new RuntimeException("UserManagement plugin not loaded"))
  case class GoogleUser(email: String, firstName: String)
  case class Profile(userId: String, shortName: String, nickName: String)
  case class SessionState(sessionId: Option[String], googleUser: Option[GoogleUser], profile: Option[Profile])
}
class UserManagement(implicit app: Application) extends Plugin {

  lazy val sessionEmails = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("session-emails")
  lazy val sessionTokens = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("session-tokens")

  def getGoogleUser(email: String)(implicit ec: ExecutionContext): Future[Option[GoogleUser]] = {
    for {
      googleUserXml <- DuelsInterface.duelsInterface.holder.post(
        <query xmlns='http://basex.org/rest'>
          <text>
            <![CDATA[declare variable $email as xs:string external;
              (/google-user[@email = $email and @given-name])[1]
              ]]>
          </text>
          <variable name="email" value={email}/>
        </query>)
    } yield {
      for {
        x <- Try(googleUserXml.xml).toOption
        email = x \@ "email"
        givenName = x \@ "given-name"
      } yield GoogleUser(email = email, firstName = givenName)
    }
  }

  def getProfile(email: String)(implicit ec: ExecutionContext): Future[Option[Profile]] = {
    for {
      profileXml <- DuelsInterface.duelsInterface.holder.post(
        <query xmlns='http://basex.org/rest'>
          <text>
            <![CDATA[declare variable $email as xs:string external;
              (/profile[@email = $email and @user-id and @short-name and @nick-name])[1]
              ]]>
          </text>
          <variable name="email" value={email}/>
        </query>)
    } yield {
      for {
        x <- Try(profileXml.xml).toOption
        userId = x \@ "user-id"
        shortName = x \@ "short-name"
        nickName = x \@ "nick-name"
      } yield Profile(userId = userId, shortName = shortName, nickName = nickName)
    }
  }

  def getSessionState[T](request: Request[T])(implicit ec: ExecutionContext): Future[SessionState] = {
    val sessionIdO = request.cookies.get(controllers.Duelgg.SESSION_ID).map(_.value)
    val sessionStateO =
      for {
        sessionId <- sessionIdO
        sessionEmail <- Option(sessionEmails.get(sessionId))
      } yield for {
        googleUserO <- getGoogleUser(sessionEmail)
        profileO <- getProfile(sessionEmail)
      } yield SessionState(sessionIdO, googleUserO, profileO)
    sessionStateO.getOrElse(Future{SessionState(sessionIdO, None, None)})
  }

  def mainUrl(implicit request : play.api.mvc.RequestHeader) =
    routes.Duelgg.oauth2callback().absoluteURL()

  lazy val dbName = DuelsInterface.duelsInterface.dbName

  val clientSecret = "***REMOVED***"

  val clientId = "***REMOVED***"

  def authUrl(tokenValue: String)(implicit request : play.api.mvc.RequestHeader) = {
    s"""https://accounts.google.com/o/oauth2/auth?scope=profile%20email&state=$tokenValue&""" +
      s"""redirect_uri=$mainUrl&response_type=code&""" +
      s"""client_id=$clientId&""" +
      """access_type=offline"""
  }

  def acceptOAuth(code: String)(implicit request : play.api.mvc.RequestHeader,  ec: ExecutionContext): Future[GoogleUser] = {


    val A = for {
      verifyTokenResponse <- WS.url("https://accounts.google.com/o/oauth2/token").post(Map(
        "code" -> Seq(code),
        "client_id" -> Seq(clientId),
        "client_secret" -> Seq(clientSecret),
        "redirect_uri" -> Seq(mainUrl),
        "grant_type" -> Seq("authorization_code")
      ))
    } yield (verifyTokenResponse.json \ "access_token").asOpt[JsString].map(_.value) match {
        case None => Bad(One(s"Could not find an access token at ${verifyTokenResponse.body}"))
        case Some(token) =>
          Good {
            for {
              personProfileResponse <- WS.url("https://content.googleapis.com/plus/v1/people/me")
                .withHeaders("Authorization" -> s"Bearer $token")
                .get()

              personProfileJson = personProfileResponse.json

              emailO = {
                val emails = for {
                  emailItem <- (personProfileJson \ "emails").asOpt[JsArray].toSeq.flatten(_.value)
                  JsString(emailValue) <- (emailItem \ "value").asOpt[JsString]
                  JsString(emailType) <- (emailItem \ "type").asOpt[JsString]
                } yield emailType -> emailValue
                emails.collectFirst { case ("account", email) if email.nonEmpty => email}
              }

              displayNameO = (personProfileJson \ "displayName").asOpt[JsString].map(_.value)

              givenNameO = (personProfileJson \ "name" \ "givenName").asOpt[JsString].map(_.value)

            } yield emailO match {
              case None => Bad(One(s"Could not find an e-mail in object ${personProfileResponse.body}"))
              case Some(email) =>
                Good {
                  val pushXml = <google-user xmlns="" email={email} display-name={displayNameO.orNull} given-name={givenNameO.orNull}>
                    <original-json>
                      {PCData(personProfileResponse.body)}
                    </original-json>
                  </google-user>
                  val pushXQuery = <query xmlns="http://basex.org/rest">
                    <text>{PCData(s"""
        declare variable $$email as xs:string external;
        if ( empty(db:open("$dbName")/google-user[@email = $$email]) )
        then (db:add("$dbName", ., "online-signup"))
        else ()
        """)}</text>
                    <variable name="email" value={email}/>
                    <context>{pushXml}</context>
                  </query>

                  for {
                    r <- DuelsInterface.duelsInterface.holder.post(pushXQuery)
                    _ = { if ( r.status != 200 ) { throw new RuntimeException(r.statusText+r.body) } }
                    googleUserO <- getGoogleUser(email)
                  } yield googleUserO
                }
            }
          }
      }

    A.flatMap(_.get.flatMap(_.get.map(_.getOrElse(throw new RuntimeException("Google User not present in database, expected to be.")))))
  }

}