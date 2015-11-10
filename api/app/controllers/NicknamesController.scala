package controllers

import javax.inject._

import play.api.libs.json.{JsString, JsArray}
import play.api.mvc.{Action, Controller}
import services.games.GamesService

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class NicknamesController @Inject()(gamesService: GamesService)
                                   (implicit executionContext: ExecutionContext) extends Controller {

  def getNicknames = Action.async {
    async {
      await(gamesService.loadGamesFromDatabase)
      Ok(JsArray(gamesService.gamesAgt.get().games.valuesIterator.flatMap(_.players).toSet.toList.map(JsString.apply)))
    }
  }

}