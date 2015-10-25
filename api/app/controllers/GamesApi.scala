package controllers

import javax.inject._

import akka.agent.Agent
import akka.stream.scaladsl.{Flow, Sink}
import de.heikoseeberger.akkasse.ServerSentEvent
import gcc.enrichment.{Enricher, PlayerLookup}
import gcc.game.GameReader
import gg.duel.query._
import modules.UpstreamGames
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

case class PlayerCondition(player: Set[String], user: Set[String], clan: Set[String]) {
  def on(game: SimpleGame): Boolean = {
    {player.isEmpty && user.isEmpty && clan.isEmpty} || {
      (game.users & user).nonEmpty ||
        (game.players & player).nonEmpty ||
        (game.clans & clan).nonEmpty
    }
  }
}

case class SimpleGame(id: String, gameJson: String, enhancedJson: String, enhancedNativeJson: JsObject, gameType: String,
                       users: Set[String], clans: Set[String], players: Set[String]) {
  def toEvent = Event(
    name = Option(gameType),
    id = Option(id),
    data = enhancedJson
  )
}

@Singleton
class GamesApi @Inject()(upstreamGames: UpstreamGames)(implicit executionContext: ExecutionContext, wsClient: WSClient) extends Controller {

  def index = TODO

  def getNicknames = Action {
    Ok(JsArray(gamesAgt.get().games.valuesIterator.flatMap(_.players).toSet.toList.map(JsString.apply)))
  }

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = null
    override def lookupClanId(nickname: String, atTime: DateTime): String = null
  }

  case class Games
  (games: Map[String, SimpleGame]) {
    def withNewGame(simpleGame: SimpleGame): Games = {
      copy(games = games + (simpleGame.id -> simpleGame))
    }
  }
  object Games {
    def empty: Games = Games(
      games = Map.empty
    )
  }

  val gamesAgt = Agent(Games.empty)
  val gameReader = new GameReader()
  val enricher = new Enricher(playerLookup)

  def sseToSimpleGame = Flow.apply[ServerSentEvent].mapConcat {
    sse =>
      sse.id.map { id =>
        try {
          val richJson = enricher.enrichJsonGame(sse.data)
          val richNativeJson = Json.parse(richJson).asInstanceOf[JsObject]
          val gameType = (richNativeJson \ "type").get.as[String]
          import collection.JavaConverters._
          val rsg = SimpleGame(
            id = id,
            gameJson = sse.data,
            enhancedJson = richJson,
            enhancedNativeJson = richNativeJson,
            gameType = gameType,
            users = gameReader.getUsers(richJson).asScala.collect{ case x: String => x }.toSet,
            clans = gameReader.getClans(richJson).asScala.collect{ case x: String => x }.toSet,
            players = gameReader.getPlayers(richJson).asScala.collect{ case x: String => x }.toSet
          )
          rsg
        } catch {
          case x: Throwable =>
            Logger.error("K, inside enricher loop this problem happened", x)
            throw x
        }
      }.toList
  }
  
  upstreamGames.allAndNewClient.createStream(sseToSimpleGame.to(Sink.foreach { game => gamesAgt.sendOff(_.withNewGame(game))}))

  def games(condition: TimingCondition, playerCondition: controllers.PlayerCondition) = Action {
    val matchedGames: Vector[SimpleGame] = condition.onVector(
      vector = gamesAgt.get().games.values.filter(playerCondition.on).toVector,
      t = (g: SimpleGame) => g.id,
      limit = 25
    )
    Ok(
      JsArray(matchedGames.map(_.enhancedNativeJson))
    )
  }

  def ctfGames(condition: TimingCondition, playerCondition: controllers.PlayerCondition) = Action {
    val matchedGames: Vector[SimpleGame] = condition.onVector(
      vector = gamesAgt.get().games.values.filter(playerCondition.on).toVector.filter(_.gameType == "ctf"),
      t = (g: SimpleGame) => g.id,
      limit = 25
    )
    Ok(
      JsArray(matchedGames.map(_.enhancedNativeJson))
    )
  }


  def duels(condition: TimingCondition, playerCondition: controllers.PlayerCondition) = Action {
    val matchedGames: Vector[SimpleGame] = condition.onVector(
      vector = gamesAgt.get().games.values.filter(playerCondition.on).toVector.filter(_.gameType == "duel"),
      t = (g: SimpleGame) => g.id,
      limit = 25
    )
    Ok(
      JsArray(matchedGames.map(_.enhancedNativeJson))
    )
  }

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = Action {
    val gamesMap = gameIds.gameIds.flatMap(gameId => gamesAgt.get().games.get(gameId.gameId)).map(sg =>
      sg.id -> sg.enhancedNativeJson).toMap
    if ( gamesMap.isEmpty ) NotFound("Nothing matching found.")
    else Ok(JsObject(gamesMap))
  }

  def gameById(gameId: GameId) = Action {
    gamesAgt.get().games.get(gameId.gameId) match {
      case Some(simpleGame) => Ok(simpleGame.enhancedNativeJson)
      case None => NotFound("Not found.")
    }
  }

  def newGames = Action {
    Ok.feed(
      content = newGamesEnum
    ).as("text/event-stream")
  }

  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[Event]

  upstreamGames.newClient.createStream(sseToSimpleGame.to(Sink.foreach(game => newGamesChan.push(game.toEvent))))

}