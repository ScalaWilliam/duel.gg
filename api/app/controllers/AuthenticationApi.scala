package controllers

import java.time.{ZonedDateTime, LocalDateTime}
import javax.inject._
import play.api.data._
import play.api.data.Forms._

import com.maxmind.geoip.LookupService
import controllers.authentication.{AuthenticationUser, CookieParser, GoogleToken, SignedParser}
import gg.duel.uservice.player.RegisterPlayer
import modules.PlayerClanManager
import play.api.{Logger, Configuration}
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, RequestHeader}

import scala.async.Async
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationApi @Inject()(wSClient: WSClient, configuration: Configuration,
                                  playerClanManager: PlayerClanManager)(implicit executionContext: ExecutionContext) extends Controller {
  lazy val cp = CookieParser(salt = salt)
  val defaultExpiryDays = configuration.getInt("gg.duel.api.auth.default-expiry-days").get
  val expirySoonDays = configuration.getInt("gg.duel.api.auth.expiry-soon-days").get
  val cookieName = configuration.getString("gg.duel.api.auth.cookie-name").get
  val aud = configuration.getString("gg.duel.api.auth.aud").get
  val salt = configuration.getString("gg.duel.api.auth.salt").get
  val lookupService = new LookupService("resources/GeoLiteCityv6.dat")
  val signedParser = SignedParser(salt = salt)
  val userRegistrationForm = Form(
    mapping(
      "id" -> text,
      "nickname" -> text,
      "authToken" -> text
    )(UserRegistration.apply)(UserRegistration.unapply)
  )

  def register = Action.async { implicit req =>
    Async.async {
      userRegistrationForm.bindFromRequest().value match {
        case None => BadRequest("Could not find a good form")
        case Some(form) =>
          form.validAuthToken match {
            case Some(auth) =>
              Async.await {
                playerClanManager.registerPlayer(
                  registerPlayer = RegisterPlayer(
                    id = form.id,
                    google = auth.google,
                    nickname = form.nickname,
                    countryCode = auth.countryCode.getOrElse("UN"),
                    startTime = Option.empty
                  ),
                  registerTime = ZonedDateTime.now()
                )
              } match {
                case Left(reason) =>
                  BadRequest(s"Failed to register due to: $reason")
                case Right(playerAndClan) =>
                  Ok(Json.toJson(playerAndClan))
              }
            case None =>
              BadRequest("Invalid auth token.")
          }
      }
    }
  }

  case class UserRegistration(id: String, nickname: String, userToken: String) {
    def validAuthToken = {
      PartialFunction.condOpt(userToken) {
        case signedParser.fromSignedString(authenticationUser) if !authenticationUser.isExpired =>
          authenticationUser
      }
    }
  }

  def requestCountryCode(requestHeader: RequestHeader): Option[String] = {
    Option(lookupService.getLocationV6(requestHeader.remoteAddress))
      .flatMap(location => Option(location.countryCode))
  }

  def validateGoogle(requestHeader: RequestHeader): Future[Option[AuthenticationUser]] = {
    Async.async {
      requestHeader.headers.get("X-Google-Id-Token") orElse requestHeader.getQueryString("google-id-token") match {
        case None => None
        case Some(id_token) =>
          val req = wSClient.url("https://www.googleapis.com/oauth2/v3/tokeninfo").withQueryString("id_token" -> id_token).get()
          val res = Async.await(req)
          PartialFunction.condOpt(res.json) {
            case GoogleToken.fromJson(googleToken) if googleToken.isValid(expectedAud = aud) =>
              AuthenticationUser(
                google = googleToken.email,
                countryCode = requestCountryCode(requestHeader),
                id = Option.empty,
                expires = LocalDateTime.now().plusDays(7)
              )
          }
      }
    }
  }

  def checkAuthentication = Action.async { request =>
    Async.async {
      Async.await(RequestToAuth(request)) match {
        case Some(auth) => {
          request.getQueryString("callback") match {
            case Some(cb) =>
              Ok(Jsonp(padding = cb, json = auth.toJsonAndCompact(salt = salt)))
            case None =>
              Ok(auth.toJsonAndCompact(salt = salt))
          }
        } withCookies {
          auth.asCookie(cookieName = cookieName, salt = salt)
        }
        case None => Forbidden("Could not authenticate you.")
      }
    }
  }

  object RequestToAuth {
    def apply(requestHeader: RequestHeader): Future[Option[AuthenticationUser]] = {
      Async.async {
        requestHeader.cookies.get(cookieName) match {
          case Some(cp(validToken)) if validToken.expiresSoon(days = expirySoonDays) =>
            Option(validToken.newExpiry(defaultExpiryDays))
          case Some(cp(validToken)) if !validToken.isExpired =>
            Option(validToken)
          case _ =>
            Async.await(validateGoogle(requestHeader))
        }
      }
    }
  }

}