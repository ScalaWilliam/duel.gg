package controllers

import javax.inject._

import gg.duel.query.{QueryCondition, GameType}
import services.games.GamesService

import scala.concurrent.ExecutionContext

@Singleton
class CtfController @Inject()(gamesService: GamesService)(implicit executionContext: ExecutionContext) extends AbstractGamesController(gamesService) {

  override protected def filterQueryCondition(queryCondition: QueryCondition) = {
    queryCondition.copy(
      gameType = queryCondition.gameType match {
        case g: GameType.Ctf => g
        case o => GameType.CtfOnly
      })
  }

}