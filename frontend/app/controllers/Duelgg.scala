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

  def search = stated {
    r => implicit s =>
      val term = r.queryString.get("q").toList.flatten.headOption
      async {
        Ok(views.html.search(term))
      }
  }

  def dynamicSearch = stated {
    r => _ =>
      val term = r.queryString.get("term").toList.flatten.headOption.get
      val page = r.queryString.get("page").toList.flatten.headOption.flatMap(s => Try(s.toInt).toOption).getOrElse(1)
      val query = <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text><![CDATA[
declare variable $term as xs:string external;
declare variable $duel-page as xs:int external;
declare variable $duel-limit as xs:int external;
declare variable $required-anonymous-games as xs:int external;
(:declare option output:method "json";:)
(:
List:
* Matching registered players (& some kind of emblem?)
* Other matching players
* Matching duels of the above
:)
let $lt := lower-case($term)
let $registered-users :=
  for $ru in /registered-user
  where some $nickname in $ru/nickname satisfies contains(lower-case($nickname), $lt)
  return $ru
let $other-nicks :=
  for $name in /duel/player/@name
  where not(some $nickname in $registered-users/nickname satisfies $nickname = data($name))
  where contains(lower-case($name), $lt)
  let $nom := data($name)
  group by $nom
  where count($name) ge $required-anonymous-games
  order by count($name) descending
  return $nom
let $all-nicks := (data($registered-users/nickname), $other-nicks)
let $matching-duels :=
  for $duel in /duel
  where $duel/player/@name = $all-nicks
  order by $duel/@start-time descending
  return $duel
let $start-from := 1 + (($duel-page - 1) * $duel-limit)
let $end-at := $start-from + $duel-limit
let $duel-count := count($matching-duels)
let $has-more := $duel-count > $end-at
let $reduced-duels := $matching-duels[position() = $start-from to $end-at]
return map{
  "registered-users": array {
    for $ru in $registered-users
    return map {
      "id": data($ru/@id),
      "game-nickname": data($ru/@game-nickname),
      "name": data($ru/@name)
    }
  },
  "other-nicknames": array {
    for $nick in $other-nicks
    return map {
      "nick": $nick
    }
  },
  "duels": map {
    "page": $duel-page,
    "count": $duel-count,
    "has-more": $has-more,
    "start-at": $start-from,
    "end-at": $end-at,
    "items": array {
      for $duel in $reduced-duels
      return map {
        "id": data($duel/@int-id),
        "at-time": adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ()),
        "left-player-score": data($duel/player[1]/@frags),
        "left-player-name": data($duel/player[1]/@name),
        "right-player-score": data($duel/player[2]/@frags),
        "right-player-name": data($duel/player[2]/@name),
        "mode": data($duel/@mode),
        "map": data($duel/@map)
      }
    }
  }
}

]]>
        </rest:text>
        <rest:variable name="term" value={term}/>
        <rest:variable name="duel-page" value={page.toString}/>
        <rest:variable name="required-anonymous-games" value="10"/>
        <rest:variable name="duel-limit" value="25"/>
      </rest:query>
      async {
        Ok(await(BasexProviderPlugin.awaitPlugin.query(query)).body)
      }
  }

  def searchPlayers = stated {
    r => _ =>
      val term = r.queryString.get("nickname").toList.flatten.headOption.get
      val query = <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text><![CDATA[
declare variable $term as xs:string external;
declare option output:method "json";
let $lt := lower-case($term)
let $noms :=
for $pn in /duel/player/@name
let $nom := data($pn)
where contains(lower-case($nom), $lt)
group by $nom
let $cnt := count($pn)
where $cnt ge 5
order by $cnt descending
return $nom
return
  if ( count($noms) > 50 ) then ( map{"terms": array{subsequence($noms,1,50)}, "hasMore": true()} )
  else (map{"terms": array{$noms}, "hasMore": false()})
]]>
        </rest:text>
        <rest:variable name="term" value={term}/>
      </rest:query>
      async {
        Ok(await(BasexProviderPlugin.awaitPlugin.query(query)).body)
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
//        val servers = await(DataSourcePlugin.plugin.getServers)
        await(DataSourcePlugin.plugin.getIndex) match {
          case Some(data) => Ok(views.html.homepage(data))
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