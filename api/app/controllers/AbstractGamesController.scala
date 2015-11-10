package controllers

import gg.duel.query._
import play.api.libs.json.JsArray
import play.api.mvc.{Action, Controller}
import services.games.GamesService

import scala.async.Async._
import scala.concurrent.ExecutionContext

abstract class AbstractGamesController(gamesService: GamesService)
                                      (implicit executionContext: ExecutionContext) extends Controller {

  protected def filterQueryCondition(queryCondition: QueryCondition) = queryCondition

  private implicit class fqc(queryCondition: QueryCondition) {
    def filter = filterQueryCondition(queryCondition)
  }

  def getAllGames(queryCondition: QueryCondition) = Action.async {
    async {
      Ok.chunked(await(gamesService.awaitEnumeratedFilter(queryCondition.filter)))
    }
  }

  def gamesBefore(id: GameId, queryCondition: QueryCondition, limit: LimitCondition) =
    Action.async {
      async {
        val games = await(gamesService.awaitFilter(queryCondition.filter))
        val n = limit match {
          case DefaultLimit => 5
          case SpecificLimit(l) => l
        }
        games.indexWhere(_.id == id.gameId) match {
          case -1 => NotFound("Focus game not found")
          case index =>
            val sortedGames = games.slice(index - n, index).reverse
            Ok(JsArray(sortedGames.map(_.enhancedNativeJson)))
        }
      }
    }

  def recentGames(queryCondition: QueryCondition, limitCondition: LimitCondition) =
    Action.async {
      async {
        val games = await(gamesService.awaitFilter(queryCondition.filter))
        val n = limitCondition match {
          case DefaultLimit => 5
          case SpecificLimit(l) => l
        }
        val sortedGames = games.takeRight(n).reverse
        Ok(JsArray(sortedGames.map(_.enhancedNativeJson)))
      }
    }


}
