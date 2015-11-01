package controllers

import java.time.ZonedDateTime
import javax.inject._

import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import de.heikoseeberger.akkasse.ServerSentEvent
import gcc.enrichment.{DemoLookup, Enricher, PlayerLookup}
import gcc.game.GameReader
import gg.duel.SimpleGame
import gg.duel.query._
import modules._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions


@Singleton
class GamesApi @Inject()(gamesService: GamesService)
                        (implicit executionContext: ExecutionContext) extends Controller {

  def index = TODO

  def getNicknames = Action {
    Ok(JsArray(gamesService.gamesAgt.get().games.valuesIterator.flatMap(_.players).toSet.toList.map(JsString.apply)))
  }

  def focusGames(focus: Focus, gameType: GameType, id: GameId, playerCondition: PlayerCondition, tagFilter: TagFilter,
                 serverFilter: ServerFilter) = Action {

    val games = gamesService.gamesAgt.get().games.valuesIterator
      .filter(gameType)
      .filter(serverFilter)
      .filter(playerCondition)
      .filter(tagFilter)
      .toList.sortBy(_.id)

    games.indexWhere(_.id == id.gameId) match {
      case -1 =>
        NotFound("Focus game not found")
      case index =>
        import SimpleFocusResult._
        import MultipleFocusResult._
        Ok {
          games.map(_.enhancedNativeJson).splitAt(index) match {
            case (previousGames, currentGame :: nextGames) =>
              focus match {
                case SimpleFocus =>
                  Json.toJson(SimpleFocus.collect(
                    previous = previousGames.reverse.toVector,
                    focus = currentGame,
                    next = nextGames.toVector
                  ))
                case mf: MultipleFocus =>
                  Json.toJson(mf.collect(
                    previous = previousGames.reverse.toVector,
                    focus = currentGame,
                    next = nextGames.toVector
                  ))
              }
          }
        }
    }
  }


  def timedGames(gameType: GameType, timing: TimingCondition, playerCondition: PlayerCondition,
                 limitCondition: LimitCondition, tagFilter: TagFilter, serverFilter: ServerFilter) = Action {

    var games = gamesService.gamesAgt.get().games.valuesIterator
      .filter(gameType)
      .filter(serverFilter)
      .filter(tagFilter)
      .filter(playerCondition)
      .toList.sortBy(_.id)

    if ( timing == Recent ) games = games.reverse

    val limit = limitCondition match {
      case DefaultLimit => 5
      case SpecificLimit(l) => l
    }

    val gl = games.take(limit)

    Ok(JsArray(gl.map(g => Json.toJson(g.enhancedNativeJson))))
  }

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = Action {
    val gamesMap = gameIds.gameIds.flatMap(gameId => gamesService.gamesAgt.get().games.get(gameId.gameId)).map(sg =>
      sg.id -> sg.enhancedNativeJson).toMap
    if (gamesMap.isEmpty) NotFound("Nothing matching found.")
    else Ok(JsObject(gamesMap))
  }

  def gameById(gameId: GameId) = Action {
    gamesService.gamesAgt.get().games.get(gameId.gameId) match {
      case Some(simpleGame) => Ok(simpleGame.enhancedNativeJson)
      case None => NotFound("Not found.")
    }
  }

  def newGames = Action {
    Ok.feed(
      content = gamesService.newGamesEnum
    ).as("text/event-stream")
  }

}