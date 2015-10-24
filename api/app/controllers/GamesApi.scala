package controllers

import javax.inject._

import akka.agent.Agent
import akka.stream.scaladsl.{Flow, Sink}
import de.heikoseeberger.akkasse.ServerSentEvent
import gcc.enrichment.{Enricher, PlayerLookup}
import gg.duel.query._
import modules.UpstreamGames
import org.joda.time.DateTime
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
  
  case class SimpleGame(id: String, gameJson: String, enhancedJson: String)
  
  val games = Agent(Map.empty[String, SimpleGame])

  val enricher = new Enricher(playerLookup)
  
  upstreamGames.allClient.createStream(Flow.apply[ServerSentEvent].take(5).mapConcat {
    sse =>
      sse.id.map { id =>
        val rsg = SimpleGame(
          id = id,
          gameJson = sse.data,
          enhancedJson = enricher.enrichJsonGame(sse.data)
        )
        rsg
      }.toList
  }.to(Sink.foreach { game => games.sendOff(_ + (game.id -> game)) }))

  def games(condition: TimingCondition) = TODO

  def allGames = Action {
    Ok(games.get().values.map(_.enhancedJson).mkString(
      start = "[",
      sep = ",\n",
      end = "]"
    )).as("application/json")
  }

  def ctfGames(condition: TimingCondition) = TODO

  def duels(condition: TimingCondition) = TODO

  def clanwars(condition: TimingCondition) = TODO

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = TODO

  def gameById(gameId: GameId) = TODO

}