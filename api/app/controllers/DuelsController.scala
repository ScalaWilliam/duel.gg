package controllers

import javax.inject._

import gg.duel.query._
import services.games.GamesService

import scala.concurrent.ExecutionContext


@Singleton
class DuelsController @Inject()(gamesService: GamesService)
                               (implicit executionContext: ExecutionContext) extends AbstractGamesController(gamesService) {

  override protected def filterQueryCondition(queryCondition: QueryCondition) = {
    queryCondition.copy(gameType = GameType.Duel)
  }
}