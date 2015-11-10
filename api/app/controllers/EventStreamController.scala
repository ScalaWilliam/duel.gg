package controllers

import gg.duel.query.QueryCondition
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import services.games.GamesService
import services.live.RabbitSource

import scala.concurrent.ExecutionContext


class EventStreamController @javax.inject.Inject()(gamesService: GamesService, rabbitSource: RabbitSource)
                                                  (implicit executionContext: ExecutionContext) extends Controller {

  def newGames(queryCondition: QueryCondition) = Action {
    Ok.feed(
      content = gamesService.newGamesEnum.flatMap {
        case (Some(sg), evt) if queryCondition(sg) =>
          Enumerator(evt)
        case (None, evt) => Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }


  def liveGames(queryCondition: QueryCondition) = Action {
    Ok.feed(
      content = gamesService.liveGamesEnum.flatMap {
        case (Some(sg), evt) if queryCondition(sg) =>
          Enumerator(evt)
        case (None, evt) => Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }

}
