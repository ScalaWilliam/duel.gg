package controllers

import javax.inject._

import akka.agent.Agent
import gcc.enrichment.{Enricher, PlayerLookup}
import gg.duel.indexing.OurGame
import gg.duel.query._
import modules.UpstreamGames
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import serializers._

import scala.async.Async
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.util.control.NonFatal

case class Game(gameJson: JsValue, ourGame: OurGame) {

}
object Game {

  implicit val gameReads: Reads[Game] = new Reads[Game] {
    override def reads(json: JsValue): JsResult[Game] = {
      OurGame.fromJson(json).map { og => Game(json, og) }
    }
  }
  def fromJsonString(jsString: String): Game = {
    try Json.fromJson[Game](Json.parse(jsString)).get
    catch {case NonFatal(e) =>

    println(s"Failed to process Game $jsString")
      throw e}
  }
}

case class Games(games: Map[String, Game]) {
  def isEmpty = games.isEmpty
}

object Games {
  def empty = Games(games = Map.empty)
  import Game.gameReads
  implicit val gamesReads = Json.reads[Games]

  def fromJsonStr(jsonStr: String): Games = {
    Json.fromJson[Games](Json.parse(jsonStr)).get
  }
}

case class PlayerNickname(nickname: String)

case class Player(nickname: PlayerNickname, clan: Option[String])

case class Players(players: Map[String, Player])

object Players {
  def empty = Players(players = Map.empty)

  implicit val playerNicknameReads = Json.reads[PlayerNickname]
  implicit val playerReads = Json.reads[Player]
  implicit def playersReads(implicit playersMapReads: Reads[Map[String, Player]]): Reads[Players] = new Reads[Players] {
    override def reads(json: JsValue): JsResult[Players] = playersMapReads.reads(json).map(v => Players(v))
  }

  def fromJson(jsValue: JsValue): Players = {
    Json.fromJson[Players](jsValue).get
  }
}

case class ClanPlayer(nickname: String)

case class Clan(players: Map[String, ClanPlayer])

case class Clans(clans: Map[String, Clan])

object Clans {
  def empty = Clans(clans = Map.empty)

  implicit val clanPlayerReads = Json.reads[ClanPlayer]
  implicit val clanReads = Json.reads[Clan]
  implicit def clansReads(implicit clansMapReads: Reads[Map[String, Clan]]): Reads[Clans] = new Reads[Clans] {
    override def reads(json: JsValue): JsResult[Clans] = clansMapReads.reads(json).map(j => Clans(j))
  }

  def fromJson(jsValue: JsValue): Clans = {
    Json.fromJson[Clans](jsValue).get
  }
}

trait QueryInterface {
  def queryGamesById(multipleByIdQuery: MultipleByIdQuery): Games

  def queryTimedGames(multipleTimedQuery: MultipleTimedQuery): Games

  def queryGameById(gameId: GameId): Option[Game]
}

@Singleton
class Main @Inject()(upstreamGames: UpstreamGames)(implicit executionContext: ExecutionContext, wsClient: WSClient) extends Controller {

  def index = TODO

  val agt = Agent(Games.empty)

  val playersAgt = Agent(Players.empty)
  val clansAgt = Agent(Clans.empty)

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String =
      playersAgt.get().players.collectFirst { case (id, player) if player.nickname.nickname == nickname => id }.orNull

    override def lookupClanId(nickname: String, atTime: DateTime): String =
      playersAgt.get().players.collectFirst { case (id, player) if player.nickname.nickname == nickname => player.clan }.flatten.orNull
  }

  val enricher = new Enricher(playerLookup)

  Async.async {
    val playersResponse = Async.await(wsClient.url( """http://alfa.duel.gg/api/players/players/""").get())
    Async.await(playersAgt.alter(Players.fromJson(playersResponse.json)))
    println("Done up players.")
    val clansResponse = Async.await(wsClient.url( """http://alfa.duel.gg/api/players/clans/""").get())
    Async.await(clansAgt.alter(Clans.fromJson(clansResponse.json)))
    val gamesResponse = Async.await(wsClient.url( """http://alfa.duel.gg/api/games/recent/""").get())
    val games = gamesResponse.json match {
      case JsObject(stuff) =>
        Games{stuff.mapValues{jObject =>
          Game.fromJsonString(enricher.enrichJsonGame(Json.stringify(jObject)))
        }.toMap}
      case JsArray(stuff) =>
        Games{stuff.map{jObject =>
          Game.fromJsonString(enricher.enrichJsonGame(Json.stringify(jObject)))
        }.map(g => g.ourGame.id -> g).toMap}
    }
    Async.await(agt.alter(games))
  } onComplete { case result =>
    println(s"So, we completed fetching players/clans/recent, ant we got => $result")
  }

  def qi: QueryInterface = new QueryInterface {
    override def queryGamesById(multipleByIdQuery: MultipleByIdQuery): Games = agt.get()

    override def queryTimedGames(multipleTimedQuery: MultipleTimedQuery): Games = agt.get()

    override def queryGameById(gameId: GameId): Option[Game] = ???
  }

  def games(condition: TimingCondition) = Action {
    Ok(Json.toJson(qi.queryTimedGames(MultipleTimedQuery(AllGames, condition))))
  }

  def ctfGames(condition: TimingCondition) = Action {
    Ok(Json.toJson(qi.queryTimedGames(MultipleTimedQuery(CtfOnly, condition))))
  }

  def duels(condition: TimingCondition) = Action {
    Ok(Json.toJson(qi.queryTimedGames(MultipleTimedQuery(DuelOnly, condition))))
  }

  def clanwars(condition: TimingCondition) = Action {
    Ok(Json.toJson(qi.queryTimedGames(MultipleTimedQuery(ClanwarsOnly, condition))))
  }

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = Action {
    Ok(Json.toJson(qi.queryGamesById(gameIds)))
  }

  def gameById(gameId: GameId) = Action {
    Ok(Json.toJson(qi.queryGameById(gameId)))
  }

}