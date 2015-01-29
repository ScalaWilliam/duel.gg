package plugins

import org.scalactic._
import play.api._
import play.api.libs.json.{JsString, JsArray}
import play.api.libs.ws.WS
import play.api.mvc.Request
import plugins.BasexProviderPlugin
import plugins.RegisteredUserManager.{RegistrationDetail, SessionState, RegisteredUser, GoogleEmailAddress}
import scala.collection.immutable.::
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try

object RegisteredUserManager {
  def userManagement: RegisteredUserManager = Play.current.plugin[RegisteredUserManager]
    .getOrElse(throw new RuntimeException("RegisteredUserManager plugin not loaded"))
  case class GoogleEmailAddress(email: String)
  case class RegisteredUser(userId: String, email: String, gameName: String, shortName: String)
  case class SessionState(sessionId: Option[String], googleEmailAddress: Option[GoogleEmailAddress], profile: Option[RegisteredUser])
  case class RegisteredSession(sessionId: String, profile: RegisteredUser)
  val SESSION_ID = "SESSION_ID"
  case class RegistrationDetail(gameNickname: String, shortName: String, userId: String, email: String, countryCode: String, ip: String)
}
class RegisteredUserManager(implicit app: Application) extends Plugin {

  lazy val dbName = BasexProviderPlugin.awaitPlugin.dbName
  lazy val sessionEmails = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("session-emails")
  lazy val sessionTokens = HazelcastPlugin.hazelcastPlugin.hazelcast.getMap[String, String]("session-tokens")
  def registerUser(registrationDetail: RegistrationDetail)(implicit ec: ExecutionContext): Future[Unit] = {

    val registerXml = <rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
          declare variable $user-name as xs:string external;
declare variable $email as xs:string external;
declare variable $user-id as xs:string external;
declare variable $game-nickname as xs:string external;
declare variable $user-ip as xs:string external;
declare variable $country-code as xs:string external;
declare function local:reverse-string ($arg as xs:string?) as xs:string {
   codepoints-to-string(reverse(string-to-codepoints($arg)))
};
let $partial-ip := local:reverse-string(substring-after(local:reverse-string($user-ip), ".")) || ".x"
let $faults := (
  if ( not(exists(/duel/player[@name = $game-nickname])) )
  then ("No duel with nickname " || $game-nickname ||" found") else (),
  if ( not (exists(/duel/player[@name = $game-nickname and @partial-ip = $partial-ip])) )
  then ("No duel with nickname "||$game-nickname ||" with partial IP "||$partial-ip||" found") else (),
  if ( not (exists(/duel/player[@name = $game-nickname and @partial-ip = $partial-ip and @country-code = $country-code])) )
  then ("No duel with nickname "||$game-nickname ||" with partial IP "||$partial-ip||" and country "||$country-code||" found") else (),
  if ( exists(/registered-user[@game-nickname = $game-nickname]) )
  then ("User with nickname "||$game-nickname||" already exists") else (),
  if ( exists(/registered-user/nickname[. = $game-nickname]) )
  then ("Nickname "||$game-nickname ||" already used") else (),
  if ( exists(/registered-user[@id = $user-id]) )
  then ("User "||$user-id||" already exists") else ()
)
return if ( not(empty($faults)) ) then () else
(
db:add("]]>{dbName}<![CDATA[",
        <registered-user
        id="{$user-id}"
        game-nickname="{$game-nickname}"
        name="{$user-name}"
        country-code="{$country-code}"
        email="{$email}"
        registration-ip="{$user-ip}"
        registration-date="{current-dateTime()}"
        >
        <nickname from="2014-09-21T00:00:00.00">{$game-nickname}</nickname>
        </registered-user>,
        "online-register")
      )
        ]]></rest:text>
      <rest:variable name="game-nickname" value={registrationDetail.gameNickname}/>
      <rest:variable name="user-name" value={registrationDetail.shortName}/>
      <rest:variable name="user-id" value={registrationDetail.userId}/>
      <rest:variable name="email" value={registrationDetail.email}/>
      <rest:variable name="country-code" value={registrationDetail.countryCode}/>
      <rest:variable name="user-ip" value={registrationDetail.ip}/>
    </rest:query>
    BasexProviderPlugin.awaitPlugin.query(registerXml).map(x => if ( x.body.nonEmpty) throw new Exception("Expected empty response") else Unit)
  }

  
  def registerValidation(registrationDetail: RegistrationDetail)(implicit ec: ExecutionContext): Future[Unit Or Every[ErrorMessage]] = {
    val xmlData = <rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
declare variable $user-name as xs:string external;
declare variable $email as xs:string external;
declare variable $user-id as xs:string external;
declare variable $game-nickname as xs:string external;
declare variable $user-ip as xs:string external;
declare variable $country-code as xs:string external;
declare function local:reverse-string ($arg as xs:string?) as xs:string {
   codepoints-to-string(reverse(string-to-codepoints($arg)))
};
let $partial-ip := local:reverse-string(substring-after(local:reverse-string($user-ip), ".")) || ".x"
let $faults := (
  if ( not(exists(/duel/player[@name = $game-nickname])) )
  then ("No duel with nickname " || $game-nickname ||" found") else (),
  if ( not (exists(/duel/player[@name = $game-nickname and @partial-ip = $partial-ip])) )
  then ("No duel with nickname "||$game-nickname ||" with partial IP "||$partial-ip||" found") else (),
  if ( not (exists(/duel/player[@name = $game-nickname and @partial-ip = $partial-ip and @country-code = $country-code])) )
  then ("No duel with nickname "||$game-nickname ||" with partial IP "||$partial-ip||" and country "||$country-code||" found") else (),
  if ( exists(/registered-user[@game-nickname = $game-nickname]) )
  then ("User with nickname "||$game-nickname||" already exists") else (),
  if ( exists(/registered-user/nickname[. = $game-nickname]) )
  then ("Nickname "||$game-nickname ||" already used") else (),
  if ( exists(/registered-user[@id = $user-id]) )
  then ("User "||$user-id||" already exists") else ()
)
return
  if ( empty($faults) ) then () else (
    <faults>{
      for $fault in $faults
      return <fault>{$fault}</fault>
      }</faults>
  )
]]>
      </rest:text>
      <rest:variable name="game-nickname" value={registrationDetail.gameNickname}/>
      <rest:variable name="user-name" value={registrationDetail.shortName}/>
      <rest:variable name="user-id" value={registrationDetail.userId}/>
      <rest:variable name="email" value={registrationDetail.email}/>
      <rest:variable name="country-code" value={registrationDetail.countryCode}/>
      <rest:variable name="user-ip" value={registrationDetail.ip}/>
    </rest:query>
    BasexProviderPlugin.awaitPlugin.query(xmlData).map { x =>
      if ( x.body.isEmpty ) Good(Unit) else {
        (x.xml \\ "fault").map(_.text).toList match {
          case first :: Nil => Bad(One(first))
          case first :: rest => Bad(Every(first, rest: _*))
          case _ => Good(Unit)
        }
      }
    }
  }


  def getUserByNick(nick: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
          declare variable $nickname as xs:string external;
          data(/registered-user[nickname = $nickname]/@id)
        ]]></rest:text>
      <rest:variable name="nickname" value={nick}/></rest:query>).map(x => Option(x.body).filter(_.nonEmpty))
  }

  def registerGoogleUser(email: String, data: String)(implicit ec: ExecutionContext): Future[Unit] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
          declare variable $email as xs:string external;
          declare variable $data as xs:string external;
          if ( empty(/google-user[@email = $email]) )
          then (db:add("]]>{dbName}<![CDATA[", <google-user email="{$email}"><registration-data>{$data}</registration-data></google-user>, "online-registration"))
          else ()
        ]]></rest:text><rest:variable name="email" value={email}/><rest:variable name="data" value={data}/></rest:query>).map(x => ())
  }

  def ipExists(ip: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
declare variable $user-ip as xs:string external;
declare function local:reverse-string ($arg as xs:string?) as xs:string {
   codepoints-to-string(reverse(string-to-codepoints($arg)))
};
let $partial-ip := local:reverse-string(substring-after(local:reverse-string($user-ip), ".")) || ".x"
return not(empty(//player[@partial-ip = $partial-ip]))
]]></rest:text><rest:variable name="user-ip" value={ip}/></rest:query>).map(x => x.body == "true")
  }

  def getRegisteredUser(email: String)(implicit ec: ExecutionContext): Future[Option[RegisteredUser]] = {
    for {
      profileXml <- BasexProviderPlugin.awaitPlugin.query(
        <rest:query xmlns:rest='http://basex.org/rest'>
          <rest:text>
            <![CDATA[
            declare variable $email as xs:string external;
            (/registered-user[@id and @game-nickname and @name and @country-code and @email = $email])[1]
              ]]>
          </rest:text><rest:variable name="email" value={email}/></rest:query>)
    } yield {
      if ( profileXml.body.isEmpty ) None
      else {
        val x = profileXml.xml
        Option(
          RegisteredUser(
            userId = x\@"id",
            gameName = x\@"game-nickname",
            shortName = x\@"name",
            email=x\@"email"
          )
        )
      }
    }
  }

  def getSessionState[T](implicit request: Request[T], ec: ExecutionContext): Future[SessionState] = {
    val sessionIdO = request.cookies.get(RegisteredUserManager.SESSION_ID).map(_.value)
    val sessionStateO =
      for {
        sessionId <- sessionIdO
      sessionEmail = "william@vynar.com"
//        sessionEmail <- Option(sessionEmails.get(sessionId))
      } yield for {
        profileO <- getRegisteredUser(sessionEmail)
      } yield SessionState(sessionIdO, Option(GoogleEmailAddress(sessionEmail)), profileO)
    sessionStateO.getOrElse(Future{SessionState(sessionIdO, None, None)})
  }

  def mainUrl(implicit request : play.api.mvc.RequestHeader) =
    controllers.routes.Duelgg.oauth2callback().absoluteURL()


  val clientSecret = "***REMOVED***"

  val clientId = "***REMOVED***"

  def authUrl(tokenValue: String)(implicit request : play.api.mvc.RequestHeader) = {
    s"""https://accounts.google.com/o/oauth2/auth?scope=profile%20email&state=$tokenValue&""" +
      s"""redirect_uri=$mainUrl&response_type=code&""" +
      s"""client_id=$clientId&""" +
      """access_type=offline"""
  }

  def acceptOAuth(code: String)(implicit request : play.api.mvc.RequestHeader,  ec: ExecutionContext): Future[GoogleEmailAddress] = {
    for {
      verifyTokenResponse <- WS.url("https://accounts.google.com/o/oauth2/token").post(Map(
        "code" -> Seq(code),
        "client_id" -> Seq(clientId),
        "client_secret" -> Seq(clientSecret),
        "redirect_uri" -> Seq(mainUrl),
        "grant_type" -> Seq("authorization_code")
      ))
      token = (verifyTokenResponse.json \ "access_token").asOpt[JsString].map(_.value).getOrElse {
        throw new IllegalStateException(s"Access token not found, expected one in $verifyTokenResponse")
      }
      personProfileResponse <- WS.url("https://content.googleapis.com/plus/v1/people/me").withHeaders("Authorization" -> s"Bearer $token").get()
      personProfileJson = personProfileResponse.json
      emailAddress = {
        val emails = for {
          emailItem <- (personProfileJson \ "emails").asOpt[JsArray].toSeq.flatten(_.value)
          JsString(emailValue) <- (emailItem \ "value").asOpt[JsString]
          JsString(emailType) <- (emailItem \ "type").asOpt[JsString]
        } yield emailType -> emailValue
        emails.collectFirst { case ("account", email) if email.nonEmpty => email}
      }.getOrElse(throw new IllegalStateException("Expected an e-mail address in response, got none."))
      _ <- registerGoogleUser(emailAddress, personProfileResponse.body)
    } yield GoogleEmailAddress(emailAddress)
  }

}