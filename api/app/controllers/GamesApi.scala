package controllers

import javax.inject._

import gg.duel.query._
import modules._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.async.Async
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions


@Singleton
class GamesApi @Inject()(gamesService: GamesService)
                        (implicit executionContext: ExecutionContext) extends Controller {

  def index = TODO

  def getNicknames = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      Ok(JsArray(gamesService.gamesAgt.get().games.valuesIterator.flatMap(_.players).toSet.toList.map(JsString.apply)))
    }
  }

  def focusGames(focus: Focus, gameType: GameType, id: GameId, playerCondition: PlayerCondition, tagFilter: TagFilter,
                 serverFilter: ServerFilter) = Action.async {

    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)

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
          import MultipleFocusResult._
          import SimpleFocusResult._
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
  }


  def timedGames(gameType: GameType, timing: TimingCondition, playerCondition: PlayerCondition,
                 limitCondition: LimitCondition, tagFilter: TagFilter, serverFilter: ServerFilter) = Action.async {
Async.async {
  Async.await(gamesService.loadGamesFromDatabase)
  var games = gamesService.gamesAgt.get().games.valuesIterator
    .filter(gameType)
    .filter(serverFilter)
    .filter(tagFilter)
    .filter(playerCondition)
    .toList.sortBy(_.id)

  if (timing == Recent) games = games.reverse

  val limit = limitCondition match {
    case DefaultLimit => 5
    case SpecificLimit(l) => l
  }

  val gl = games.take(limit)

  Ok(JsArray(gl.map(g => Json.toJson(g.enhancedNativeJson))))
}
  }

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      val gamesMap = gameIds.gameIds.flatMap(gameId => gamesService.gamesAgt.get().games.get(gameId.gameId)).map(sg =>
        sg.id -> sg.enhancedNativeJson).toMap
      if (gamesMap.isEmpty) NotFound("Nothing matching found.")
      else Ok(JsObject(gamesMap))
    }
  }

  def gameById(gameId: GameId) = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      gamesService.gamesAgt.get().games.get(gameId.gameId) match {
        case Some(simpleGame) => Ok(simpleGame.enhancedNativeJson)
        case None => NotFound("Not found.")
      }
    }
  }

  def newGames(gameType: GameType, playerCondition: PlayerCondition,
               tagFilter: TagFilter, serverFilter: ServerFilter) = Action {
    Ok.feed(
      content = gamesService.newGamesEnum.flatMap{
        case (sg, evt) if gameType(sg) && playerCondition(sg) && tagFilter(sg) && serverFilter(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event] }
    ).as("text/event-stream")
  }

  def liveGames(gameType: GameType, playerCondition: PlayerCondition,
                tagFilter: TagFilter, serverFilter: ServerFilter) = Action {
    Ok.feed(
      content = gamesService.liveGamesEnum.flatMap{
        case (sg, evt) if gameType(sg) && playerCondition(sg) && tagFilter(sg) && serverFilter(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event] }
    ).as("text/event-stream")
  }

}