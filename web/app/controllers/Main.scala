package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import services.GameService

import scala.concurrent.ExecutionContext

class Main @Inject()(gameService: GameService)(implicit executionContext: ExecutionContext) extends Controller {
  def index = Action.async {
    gameService.games.map { games =>
      Ok(games.takeRight(5).mkString("\n"))
    }
  }
}
