package controllers

import java.io.File
import java.net.InetAddress
import java.util.UUID
import com.maxmind.geoip2.DatabaseReader
import play.api.mvc._
import play.twirl.api.Html
import plugins.RegisteredUserManager._
import plugins._
import scala.concurrent.Future
import scala.util.Try
import scala.async.Async.{async, await}
object Duelgg extends Controller {

  val SESSION_ID = "sessionId"
  import scala.concurrent.ExecutionContext.Implicits.global

  def viewPlayers = stated {
    _ => implicit s =>
      async {
        val r = await(DataSourcePlugin.plugin.getPlayers)
        Ok(views.html.main("Player")(Html(""))(Html(r)))
      }
  }

  def showDuel(id: Int) = stated {
    _ => implicit sess =>
      async {
        await(DataSourcePlugin.plugin.getDuelDetailedCard(id)) match {
          case Some(data) => Ok(views.html.duel(Html(data)))
          case None => NotFound("Fail")
        }
      }
  }
  def index = stated {
    _ => implicit sess =>
      async {
        val servers = await(DataSourcePlugin.plugin.getServers)
        await(DataSourcePlugin.plugin.getIndex) match {
          case Some(data) => Ok(views.html.homepage(Html(servers),Html(data)))
          case None => NotFound("Fail")
        }
      }
  }

  def viewPlayerDuel(id: String, duelId: Int) = stated {
    req => implicit sess =>
      async {
        await(DataSourcePlugin.plugin.getPlayerDuel(id, duelId)) match {
          case Some(data) =>
            val cnts = await(DataSourcePlugin.plugin.getPlayerCounts(id))
            Ok(views.html.playerDuel(Html(data)))
          case None => NotFound("Fail)")
        }
      }
  }

  def viewPlayer(id: String) = stated {
    req => implicit sess =>
      async {
        val nicknameO = req.queryString.get("nickname").toList.flatten.headOption
        val userO = Option(id).filter(_.nonEmpty)
        (userO, nicknameO) match {
          case (Some(username), _) =>
            await(DataSourcePlugin.plugin.getUsername(username)) match {
              case Some(data) =>
                val counts = await(DataSourcePlugin.plugin.getPlayerCounts(username))
                Ok(views.html.player(Html(counts.getOrElse("<p>Counts not available</p>")))(Html(data)))
              case _ => NotFound
            }
          case (_, Some(nick)) =>
            val hasUser = await(RegisteredUserManager.userManagement.getUserByNick(nick))
            hasUser match {
              case Some(user) => SeeOther(controllers.routes.Duelgg.viewPlayer(user).url)
              case None =>
                await(DataSourcePlugin.plugin.getNickname(nick)) match {
                  case Some(data) => Ok(views.html.basicPlayer(Html(data)))
                  case _ => NotFound
                }
            }
          case _ => NotFound
        }
      }
  }


  def viewMe = registeredSync { _ => (rs: RegisteredSession) =>
    SeeOther(controllers.routes.Duelgg.viewPlayer(rs.profile.userId).url)
  }

  def logout = Action { _ =>
    val newUrl = controllers.routes.Duelgg.index().url
    val emptyCookie = Cookie(RegisteredUserManager.SESSION_ID, "")
    SeeOther(newUrl).withCookies(emptyCookie)
  }

  def login = Action.async {
    implicit request =>

      val sessionId = request.cookies.get(RegisteredUserManager.SESSION_ID).map(_.value).getOrElse(UUID.randomUUID().toString)
      val sessionCookie = Cookie(RegisteredUserManager.SESSION_ID, sessionId, maxAge = Option(200000))
      val newTokenValue = UUID.randomUUID().toString
      RegisteredUserManager.userManagement.sessionTokens.put(sessionId, newTokenValue)
      val noCacheCookie = Cookie("nocache", "true", maxAge = Option(200000))
      Future { SeeOther(RegisteredUserManager.userManagement.authUrl(newTokenValue)).withCookies(sessionCookie, noCacheCookie) }
  }

  def stated[V](f: Request[AnyContent] => SessionState => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      RegisteredUserManager.userManagement.getSessionState.flatMap { implicit suzzy =>
        f(request)(suzzy)
      }
  }

  lazy val topic = HazelcastPlugin.hazelcastPlugin.hazelcast.getTopic[String]("new-users")

  def statedSync[V](f: Request[AnyContent] => SessionState => Result): Action[AnyContent] =
    stated { a => b =>
      Future{f(a)(b)}
    }

  def registeredSync[V](f: Request[AnyContent] => RegisteredSession => Result): Action[AnyContent] =
    registered { a => b =>
      Future{f(a)(b)}
    }

  def registered[V](f: Request[AnyContent] => RegisteredSession => Future[Result]): Action[AnyContent] =
    stated { implicit request => {
      case SessionState(Some(sessionId), Some(GoogleEmailAddress(email)), Some(profile)) =>
        f(request)(RegisteredSession(sessionId, profile))
      case other =>
        Future {
          SeeOther(controllers.routes.Duelgg.createProfile().url)
        }
    }
    }

  def mainUrl(implicit request : play.api.mvc.RequestHeader) =
    controllers.routes.Duelgg.oauth2callback().absoluteURL()

  def oauth2callback = Action.async {
    implicit request =>
      val code = request.queryString("code").head
      val state = request.queryString("state").head
      val sessionId = request.cookies(RegisteredUserManager.SESSION_ID).value
      val expectedState = RegisteredUserManager.userManagement.sessionTokens.get(sessionId)
      //      RegisteredUserManager.userManagement.sessionEmails.remove(sessionId)
      RegisteredUserManager.userManagement.sessionTokens.remove(sessionId)
      if ( state != expectedState ) {
        throw new RuntimeException(s"Expected $expectedState, got $state")
      }

      import play.api.libs.concurrent.Execution.Implicits.defaultContext
      for {
        user <- RegisteredUserManager.userManagement.acceptOAuth(code)
      } yield {
        RegisteredUserManager.userManagement.sessionEmails.put(sessionId, user.email)
//        NotFound
        SeeOther("/")
//       SeeOther(controllers.routes.UUse.viewMe().absoluteURL())
      }
  }

  val reader = {
    val database = new File(scala.util.Properties.userHome, "GeoLite2-Country.mmdb")
    new DatabaseReader.Builder(database).build()
  }

  def getCountryCode(ip:String): Option[String] =
    for {
      countryResponse <- Try(reader.country(InetAddress.getByName(ip))).toOption
      country <- Option(countryResponse.getCountry)
      isoCode <- Option(country.getIsoCode)
    } yield isoCode

  def getRegistrationDetail(country: String, email: String, ip:String)(implicit request: Request[AnyContent]): Option[RegistrationDetail] =
    for {
      form <- request.body.asMultipartFormData.map(_.dataParts)
      gameNickname <- form.get("game-nickname").map(_.headOption).flatten
      shortName <- form.get("short-name").map(_.headOption).flatten
      userId <- form.get("user-id").map(_.headOption).flatten
    } yield RegistrationDetail(
      email = email,
      countryCode = country,
      userId = userId,
      shortName = shortName,
      gameNickname = gameNickname, ip = ip
    )
  def createProfile = stated{implicit request => implicit state =>
    state match {
      case SessionState(_, None, _) =>
        Future{SeeOther(controllers.routes.Duelgg.login().url)}
      case SessionState(_, _, Some(_)) =>
        Future{SeeOther(controllers.routes.Duelgg.viewMe().url)}
      case _ =>
        async {
          if ( state.profile.nonEmpty ) {
            NotFound
            //            todo SeeOther(controllers.routes.Main.viewPlayer(state.profile.get.userId).url)
          }
          val ipAddress = if ( scala.util.Properties.osName == "Windows 7" ) { "77.44.45.26" } else request.remoteAddress

          getCountryCode(ipAddress) match {
            case None =>
              Ok(views.html.createProfileNotAllowed(List(s"Could not find a country code for your IP address $ipAddress.")))
            case Some(countryCode) =>
              state.googleEmailAddress match {
                case None => Ok(views.html.createProfileNotAllowed(List("You do not appear to have an e-mail address. Please sign in again.")))
                case Some(GoogleEmailAddress(emailAddress)) =>
                  val ipExists = true //await(RangerPlugin.awaitPlugin.rangeExists(ipAddress))
                  if ( !ipExists ) {
                    Ok(views.html.createProfileNotAllowed(List(s"You do not appear to have have played with your current IP $ipAddress.")))
                  } else {
                    getRegistrationDetail(countryCode, emailAddress, ipAddress) match {
                      case None =>
                        Ok(views.html.createProfile(countryCode))
                      case Some(reg) =>
                        if ( """.{3,15}""".r.unapplySeq(reg.gameNickname).isEmpty) {
                          Ok(views.html.createProfile(countryCode, List("Invalid game nickname specified")))
                        } else if ( """[A-Za-z0-9]{3,12}""".r.unapplySeq(reg.shortName).isEmpty) {
                          Ok(views.html.createProfile(countryCode, List("Invalid short name specified")))
                        } else if ( """[a-z0-9]{3,10}""".r.unapplySeq(reg.userId).isEmpty) {
                          Ok(views.html.createProfile(countryCode, List("Invalid username specified")))
                        } else {
                          val regDetail = await(RegisteredUserManager.userManagement.registerValidation(reg))
                          regDetail match {
                            case org.scalactic.Bad(stuff) =>
                              Ok(views.html.createProfile(countryCode, stuff.toList))
                            case _ =>
                              await(RegisteredUserManager.userManagement.registerUser(reg))
                              topic.publish(reg.userId)
                              //                              await(AwaitUserUpdatePlugin.awaitPlugin.awaitUser(reg.userId).recover{case _: AskTimeoutException => "whatever"})
                              NotFound
                            //                              todo SeeOther(controllers.routes.Main.viewPlayer(reg.userId).url)
                          }
                        }
                    }
                  }
              }
          }
        }
      case _ => Future { Forbidden }
    }
  }

}