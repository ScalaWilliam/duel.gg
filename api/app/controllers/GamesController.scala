package controllers
import javax.inject._

import gg.duel.query._
import play.api.libs.json.JsObject
import play.api.mvc.{Controller, Action}
import services.games.GamesService

import scala.async.Async._
import scala.concurrent.ExecutionContext


@Singleton
class GamesController @Inject()(gamesService: GamesService)
                               (implicit executionContext: ExecutionContext) extends Controller {

  def gamesByIds(gameIds: gg.duel.query.MultipleByIdQuery) = Action.async {
    async {
      await(gamesService.awaitSorted).collect {
        case (idString, game) if gameIds.gameIds.contains(GameId(idString)) =>
          idString -> game.enhancedNativeJson
      }.toMap match {
        case map if map.nonEmpty => Ok(JsObject(map))
        case _ => NotFound("No matching games found.")
      }
    }
  }

  def gameById(gameId: GameId) = Action.async {
    async {
      await(gamesService.awaitSorted).map(_._2).find(_.id == gameId.gameId) match {
        case Some(game) => Ok(game.enhancedNativeJson)
        case _ => NotFound("Game not found.")
      }
    }
  }
}
