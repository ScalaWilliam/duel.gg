package services

import javax.inject._

import lib.NicknameMatcher
import org.apache.http.client.fluent.Request
import play.api.libs.json.{JsSuccess, Json}
import play.api.{Configuration, Logger}
import services.ClansService._

object ClansService {
  case class Clan
  (id: String, tag: Option[String], name: String,
   website: Option[String], irc: Option[String],
   tags: Option[List[String]]) {
    def clanTags: List[String] = tag.toList ++ tags.toList.flatten
    def nicknameIsInClan(nickname: String): Boolean = {
      clanTags.exists(clanTag => NicknameMatcher(format = clanTag)(nickname = nickname))
    }
  }
}

@Singleton
class ClansService @Inject()(configuration: Configuration) {
  private val uri = configuration.getString("gg.duel.clans.json.uri").getOrElse("http://duel.gg/clans/json/")
  Logger.info(s"Loading clans from uri: $uri")
  private implicit val clanReads = Json.reads[Clan]
  private def jsonBody = Request.Get(uri).execute().returnContent().asString()
  val clans: Map[String, Clan] = Json.fromJson[Map[String, Clan]](Json.parse(jsonBody)) match {
    case JsSuccess(c, _) => c
    case o =>
      Logger.error(s"Could not parse list of clans: $o. Using empty list.")
      Map.empty
  }

  Logger.info(s"Got list of clans: ${clans.keySet}")
}