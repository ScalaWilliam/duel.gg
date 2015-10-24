package controllers

import javax.inject._

import gcc.enrichment.{Enricher, PlayerLookup}
import gg.duel.query._
import modules.UpstreamGames
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

@Singleton
class GamesApi @Inject()(upstreamGames: UpstreamGames)(implicit executionContext: ExecutionContext, wsClient: WSClient) extends Controller {

  def index = TODO

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = ???

    override def lookupClanId(nickname: String, atTime: DateTime): String = ???
  }

  val enricher = new Enricher(playerLookup)

//  enricher.enrichJsonGame()

  def games(condition: TimingCondition) = TODO

  def ctfGames(condition: TimingCondition) = TODO

  def duels(condition: TimingCondition) = TODO

  def clanwars(condition: TimingCondition) = TODO

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = TODO

  def gameById(gameId: GameId) = TODO

}