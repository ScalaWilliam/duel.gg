package controllers

import java.io.File
import java.net.InetAddress
import java.util.UUID
import com.maxmind.geoip2.DatabaseReader
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.Html
import plugins.DataSourcePlugin._
import plugins.RealtimeDuelsPlugin.DuelUpdatesSenderActor
import plugins.RegisteredUserManager._
import plugins._
import scala.concurrent.Future
import scala.util.Try
import scala.async.Async.{async, await}
object Duelgg extends Controller  {

  val SESSION_ID = "sessionId"
  import scala.concurrent.ExecutionContext.Implicits.global

  def viewPlayers = stated {
    _ => implicit s =>
      async {
        val r = await(DataSourcePlugin.plugin.getPlayers)
        Ok(views.html.main("Players")(Html(""))(Html(r)))
      }
  }
  def liveStreamStatus = stated {
    r => implicit s =>
      async {
        Ok(await(RealtimeDuelsPlugin.plugin.giveStatus).toJson)
      }
  }
  import play.api.Play.current
  def liveStream = WebSocket.acceptWithActor[String, String] { request => out =>
    DuelUpdatesSenderActor.props(out)
  }

  def search = stated {
    r => implicit s =>
      val term = r.queryString.get("q").toList.flatten.headOption
      async {
        Ok(views.html.search(term))
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
          DuelStoragePlugin.plugin.currentStorage.duelszMap.get(id) match {
            case None => NotFound(s"Duel $id not found")
            case Some(theDuel) =>
              DuelStoragePlugin.plugin.currentStorage.getDuelFocus(id) match {
                case Some(data) =>
                  val json = s"""{"duel":$theDuel, "duels":[${data.mkString(", ")}]}"""
                  Ok(views.html.duel(id, json))
                case None => NotFound(s"Could not find duel $id")
              }
          }
      }
  }
  def jsonDuel(id: Int) = stated {
    _ => implicit sess =>
      async {
        val o = DuelStoragePlugin.plugin.currentStorage.getDuelFocus(id)
        Ok(s"$o")
      }
  }
  def jsonUser(userId: String) = stated {
    r => implicit sess =>
      async {
        val duelIdO = r.queryString.get("duel").flatMap(_.headOption).map(_.toInt)
        val beforeDuelIdO = r.queryString.get("before-duel").flatMap(_.headOption).map(_.toInt)
        val afterDuelIdO = r.queryString.get("after-duel").flatMap(_.headOption).map(_.toInt)
        (duelIdO, beforeDuelIdO, afterDuelIdO) match {
          case (Some(duelId), _, _) =>
            DuelStoragePlugin.plugin.currentStorage.getUserDuelsFocus(userId, duelId) match {
              case Some(duels) =>
                Ok( s"""{"duels": [${duels.mkString(", ")}]}""")
              case None => NotFound("No!")
            }
          case (_, Some(beforeDuelId), _) =>
            DuelStoragePlugin.plugin.currentStorage.getUserDuelsBefore(userId, beforeDuelId) match {
              case Some(duels) => Ok( s"""{"duels": [${duels.mkString(", ")}]}""")
              case None => NotFound("Could not ...")
            }
          case (_, _, Some(afterDuelId)) =>
            DuelStoragePlugin.plugin.currentStorage.getUserDuelsAfter(userId, afterDuelId) match {
              case Some(duels) => Ok( s"""{"duels": [${duels.mkString(", ")}]}""")
              case None => NotFound(s"Could not...")
            }
          case _ =>
            DuelStoragePlugin.plugin.currentStorage.getUserDuels(userId) match {
              case Some(ds) => Ok( s"""{"duels": [${ds.mkString(", ")}]}""")
              case None => NotFound(s"Could not...")
            }
        }
      }
  }

  def jsonSearch = stated {
    r => implicit sess =>
      async {
        val beforeDuel = r.queryString.get("duel-before").flatMap(_.headOption).map(_.toInt)
        val qO = r.queryString.get("q").flatMap(_.headOption).filter(_.length >= 3)
        qO match {
          case None => NotFound("Query string must be 3+ in length")
          case Some(q) =>
            val (matchingUsers, matchingNicknames, matchingDuels) = DuelStoragePlugin.plugin.currentStorage.search(q, beforeDuel)
            val regsJson = Json.toJson(matchingUsers.map { case (name, nicks) => Json.toJson(Map("id" -> name, "name" -> name, "gameNickname" -> nicks.head))})
            val others = Json.toJson(matchingNicknames.map(n => Json.toJson(Map("nickname" -> n))))
            val duels = s"""[${matchingDuels.mkString(", ")}]"""
            val outJson = s"""{"registeredUsers":$regsJson
,
"otherNicknames": $others
,
"duels": $duels}
"""
        Ok(outJson)
            }
      }
  }

  def jsonMain = stated {
    r => implicit sess =>
    async {
      val did = r.queryString.get("before-duel").flatMap(_.headOption)
      val daf = r.queryString.get("after-duel").flatMap(_.headOption)
      val o = did match {
        case Some(v) =>DuelStoragePlugin.plugin.currentStorage.getMainBefore(v.toInt)
        case None =>
          daf match {
            case Some(n) => DuelStoragePlugin.plugin.currentStorage.getMainAfter(n.toInt)
            case _ => DuelStoragePlugin.plugin.currentStorage.getMain
          }
      }
      Ok(s"""{"duels": [${o.mkString(", ")}]}""")
    }

//      async {
//        val q = did match {
//          case Some(v) => MainUpToDuel(v)
//          case _ => Main
//        }
//        Ok(await(DataSourcePlugin.plugin.googQ(q)).get)
//      }

  }

  def servers = stated {
    _ => implicit  s =>
      async {
        val servahs = await(DataSourcePlugin.plugin.getServers)
        Ok(views.html.servers(Html(servahs)))
      }
  }

  def index = stated {
    _ => implicit sess =>
      async {
        val article = await(DataSourcePlugin.plugin.getNewsArticle)
        val duelsJsons = DuelStoragePlugin.plugin.currentStorage.getMain
        Ok(views.html.homepage(s"""[${duelsJsons.mkString(",")}]""")(Html(article)))
      }
    }

  def viewPlayer(userId: String) = stated {
    req => implicit sess =>
      async {
        val nickname = req.queryString.get("nickname").toList.flatten.headOption
        val did = req.queryString.get("duel").flatMap(_.headOption).map(_.toInt)
        nickname match {
          case Some(nick) =>
            DuelStoragePlugin.plugin.currentStorage.getNickDuels(nick) match {
              case Some(o) =>
                val playerJson = Json.toJson(Map("nickname" -> nick))
                val json = s"""{"user": $playerJson, "duels": [${o.mkString(", ")}]}"""
                Ok(views.html.player(json, did))
              case _ =>
                NotFound(s"Could not find duels for $nick")
            }
          case None =>
            def stats = DuelStoragePlugin.plugin.currentStorage.userToMapModeCounts(userId)
            did match {
              case Some(duelId) =>
                DuelStoragePlugin.plugin.currentStorage.getUserDuelsFocus(userId, duelId) match {
                  case Some(o) =>
                    val userJson = DuelStoragePlugin.plugin.currentStorage.usersList(userId).asBasicJson
                    val json = s"""{ "user": $userJson, "stats": $stats,"duels": [${o.mkString(", ")}]}"""
                    Ok(views.html.player(json, did))
                  case None =>
                    NotFound(s"Could not find user $userId with focus $duelId")
                }
              case _ =>
                DuelStoragePlugin.plugin.currentStorage.getUserDuels(userId) match {
                  case Some(o) =>
                    val userJson = DuelStoragePlugin.plugin.currentStorage.usersList(userId).asBasicJson
                    val json = s"""{ "user": $userJson, "stats": $stats, "duels": [${o.mkString(", ")}]}"""
                    Ok(views.html.player(json, did))
                  case None =>
                    NotFound(s"Could not find user $userId")
                }
            }
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
  def drakasOnly[V](f: Request[AnyContent] => RegisteredSession => Future[Result]): Action[AnyContent] =
  registered {implicit r => {
    case rs@RegisteredSession(_, RegisteredUser("drakas", "william@vynar.com", _, _)) =>
      f(r)(rs)
    case other => Future{NotFound}
  }}
//  Action.async { req => f(req)(RegisteredSession(null, RegisteredUser("drakas", "william@vynar.com", "w00p|Drakas", "Drakas"))) }

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
        SeeOther(controllers.routes.Duelgg.viewMe().absoluteURL())
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
                              await(DuelStoragePlugin.plugin.createProfile(reg.userId))
//                              topic.publish(reg.userId)
//                              await(AwaitUserUpdatePlugin.awaitPlugin.awaitUser(reg.userId).recover{case _: AskTimeoutException => "whatever"})
                            SeeOther(controllers.routes.Duelgg.viewMe().url)
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
  def questions = stated {
    r => implicit s =>
      async {  Ok(views.html.questions(Html(await(DataSourcePlugin.plugin.getQuestions))))}
  }

  def pushArticle = drakasOnly {
    r => implicit s =>
      async {
        val isGood = await(verifyArticle(r.body.asText.get))
        if ( isGood.isGood ) {
          await(pushArticleJ(r.body.asText.get))
          Ok("""{"good":true}""")
        } else {
          Ok(isGood.swap.get)
        }
      }
  }

  import org.scalactic._
  import org.scalactic.Accumulation._
  def verifyArticle(inputJson: String): Future[Unit Or String] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest="http://basex.org/rest">
      <rest:text>
        <![CDATA[
declare variable $input-json as xs:string external;
declare option output:method 'json';
let $json-input := parse-json($input-json)
let $failures := (
  if ( empty($json-input?pageTitle) or $json-input?pageTitle = '' ) then (("Page title empty")) else (),
  if ( not(matches($json-input?pageName, '^[a-z-]+$')) ) then (("Page name not well formed")) else (),
  if ( empty($json-input?pageContent) or $json-input?pageContent = '' ) then (("Page content empty")) else ()
)
return if ( empty($failures) ) then (map{"good": true()}) else (map { "good": false(), "failures": array { $failures } })
]]>
      </rest:text>
      <rest:variable name="input-json" value={inputJson}/>
    </rest:query>).map{
      r =>
        val isGood = (r.json \ "good").as[Boolean]
        if ( isGood ) Good(Unit) else Bad(r.body)
    }
  }
  def pushArticleJ(inputJson: String): Future[Unit] = {
    BasexProviderPlugin.awaitPlugin.query{<rest:query xmlns:rest="http://basex.org/rest">
      <rest:text>
        <![CDATA[
declare variable $input-json as xs:string external;
let $json := parse-json($input-json)
let $new-article := <article title="{$json?pageTitle}" name="{$json?pageName}" publish-time="{
if ( $json?publishTime castable as xs:dateTime ) then ($json?publishTime) else (current-dateTime())
}" enabled="{$json?enabled}">{$json?pageContent}</article>
let $existing-article := /article[@name = data($json?pageName)]
return if ( empty($existing-article) ) then (db:add("duelgg", $new-article, "publish-articles")) else (replace node $existing-article  with $new-article)
]]>
      </rest:text>
      <rest:variable name="input-json" value={inputJson}/>
    </rest:query>
    }.map(_ => ())
  }

  def newsAtom = Action.async {
    r =>
      async {
        val data = await(DataSourcePlugin.plugin.getAtomFeed)
        Ok(data).withHeaders("Content-Type" -> "application/atom+xml")
      }
  }

  def showArticle(name: String) = stated {
    r => implicit s =>
      async {
        await(DataSourcePlugin.plugin.readArticle(name)) match {
          case Some(article) => Ok(views.html.main("Article!")(Html(""))(Html(article)))
          case None => NotFound(s"Article id $name not found.")
        }
      }
  }

  def controlIndex = drakasOnly {
    r => implicit s =>
      async {
      val r = await(BasexProviderPlugin.awaitPlugin.query{
        <rest:query xmlns:rest="http://basex.org/rest">
          <rest:text>
            <![CDATA[
declare option output:method 'json';
let $articles :=
  for $article in /article
  order by $article/@publish-time descending
  return map {
    "name": data($article/@name),
    "title": data($article/@title),
    "enabled": $article/@enabled = 'true',
    "publishTime": data($article/@publish-time),
    "content": data($article)
  }
let $chartItems :=
  for $d in /duel
  let $date := xs:date(adjust-dateTime-to-timezone(xs:dateTime($d/@start-time), ()))
  group by $date
  order by $date ascending
  return map { "time": $date, "value": count($d) }
let $users :=
  for $u in /registered-user
  return map { "id": data($u/@id) }
return map {
  "chartItems": array { $chartItems[position() = (last() - 50) to last()] },
  "articles": array { $articles },
  "users": array { $users }
}
]]>
          </rest:text>
        </rest:query>
      })
        Ok(views.html.admin.homepage(r.body))
      }
  }
}