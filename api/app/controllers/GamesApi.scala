package controllers

import javax.inject._

import akka.agent.Agent
import akka.stream.scaladsl.{Flow, Sink}
import de.heikoseeberger.akkasse.ServerSentEvent
import gcc.enrichment.{Enricher, PlayerLookup}
import gg.duel.query._
import modules.UpstreamGames
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

@Singleton
class GamesApi @Inject()(upstreamGames: UpstreamGames)(implicit executionContext: ExecutionContext, wsClient: WSClient) extends Controller {

  def index = TODO

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
  
  case class SimpleGame(id: String, gameJson: String, enhancedJson: String, enhancedNativeJson: JsValue)
  
  val gamesAgt = Agent(Games.empty)

  val enricher = new Enricher(playerLookup)
  
  upstreamGames.allAndNewClient.createStream(Flow.apply[ServerSentEvent].mapConcat {
    sse =>
      sse.id.map { id =>
        val richJson = enricher.enrichJsonGame(sse.data)
        val richNativeJson = Json.parse(richJson)
        val rsg = SimpleGame(
          id = id,
          gameJson = sse.data,
          enhancedJson = richJson,
          enhancedNativeJson = richNativeJson
        )
        rsg
      }.toList
  }.to(Sink.foreach { game => gamesAgt.sendOff(_.withNewGame(game))}))

  def games(condition: TimingCondition) = Action {
    condition match {
      case Recent =>
        Ok(JsArray(gamesAgt.get().games.values.toVector.sortBy(_.id).takeRight(25).map(_.enhancedNativeJson)))
      case First =>
        Ok(JsArray(gamesAgt.get().games.values.toVector.sortBy(_.id).take(25).map(_.enhancedNativeJson)))
      case _ => NotFound("Not implemented yet")
    }
  }

  def ctfGames(condition: TimingCondition) = TODO

  def duels(condition: TimingCondition) = TODO

  def clanwars(condition: TimingCondition) = TODO

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = TODO

  def gameById(gameId: GameId) = TODO

}