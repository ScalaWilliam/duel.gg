package modules

/**
 * Created on 27/08/2015.
 */

import javax.inject._

import gg.duel.uservice.clan.{Clan, CurrentPatterns, PreviousPatterns}
import gg.duel.uservice.player.{Player, CurrentNickname, PreviousNickname}
import org.h2.mvstore.MVStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import collection.JavaConverters._

import scala.concurrent.{ExecutionContext, Future, blocking}

@Singleton
class PlayerClanPersistence @Inject()(configuration: Configuration, applicationLifecycle: ApplicationLifecycle)(implicit executionContext: ExecutionContext) {

  implicit val previousNicknameFormats = Json.format[PreviousNickname]
  implicit val currentNicknameFormats = Json.format[CurrentNickname]
  implicit val playerFormats = Json.format[Player]
  implicit val previousPatternsFormats = Json.format[PreviousPatterns]
  implicit val currentPatternsFormats = Json.format[CurrentPatterns]
  implicit val clanFormats = Json.format[Clan]

  val database = MVStore.open(configuration.getString("gg.duel.players.database.name").getOrElse("players.db"))

  val clansMap = database.openMap[String, String]("clans")
  val playersMap = database.openMap[String, String]("players")
  def getClans = clansMap.asScala.toMap.mapValues(v => Json.fromJson[Clan](Json.parse(v)).get)
  def getPlayers = playersMap.asScala.toMap.mapValues(v => Json.fromJson[Player](Json.parse(v)).get)

  def putClan(clan: Clan) = {
    clansMap.put(clan.id, Json.stringify(Json.toJson(clan)))
    database.commit()
  }
  def putPlayer(player: Player) = {
    playersMap.put(player.id, Json.stringify(Json.toJson(player)))
    database.commit()
  }

  applicationLifecycle.addStopHook(() => Future(blocking(database.close())))

}
