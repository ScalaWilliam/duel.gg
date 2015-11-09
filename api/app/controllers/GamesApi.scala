package controllers

import javax.inject._

import gg.duel.WindowedSearch
import gg.duel.query._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.games.GamesService
import services.live.RabbitSource

import scala.async.Async
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions


@Singleton
class GamesApi @Inject()(gamesService: GamesService, rabbitSource: RabbitSource)
                        (implicit executionContext: ExecutionContext) extends Controller {

  def index = TODO

  def getNicknames = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      Ok(JsArray(gamesService.gamesAgt.get().games.valuesIterator.flatMap(_.players).toSet.toList.map(JsString.apply)))
    }
  }

  def directedGames(direction: LookupDirection, id: GameId, queryCondition: QueryCondition, limit: LimitCondition) =
    Action.async { implicit request =>
      Async.async {
        Async.await(gamesService.loadGamesFromDatabase)
        val limitNo = limit match {
          case DefaultLimit => 5
          case SpecificLimit(l) => l
        }
        val ml = WindowedSearch(filterGames(queryCondition))
          .FocusedLookup(sg => sg.id == id.gameId)
          .MultipleLookup(limitNo)
        val itemsSpecificLinksO = if (direction.isAfter) {
          ml.AfterLookup.apply().map { afterLookup =>
            val nextLinkO = afterLookup.laterGame.map(sg =>
              controllers.routes.GamesApi.directedGames(
                direction = LookupDirection.After,
                id = GameId(sg.id),
                queryCondition = queryCondition,
                limit = limit
              ).url)
              .map(url => s"""<$url>; rel="next"; title="Next set of games"""")
            val previousLinkO = afterLookup.laterGame.map(sg =>
              controllers.routes.GamesApi.directedGames(
                direction = LookupDirection.Before,
                id = GameId(sg.id),
                queryCondition = queryCondition,
                limit = limit
              ).url).map(url => s"""<$url>; rel="previous"; title="Previous set of games"""")
            (afterLookup.next, nextLinkO.toList ++ previousLinkO)
          }
        } else {
          ml.BeforeLookup.apply().map { beforeLookup =>
            val previousLinkO = beforeLookup.earlierGame.map(sg =>
              controllers.routes.GamesApi.directedGames(
                direction = LookupDirection.Before,
                id = GameId(sg.id),
                queryCondition = queryCondition,
                limit = limit
              ).url).map(url => s"""<$url>; rel="previous"; title="Previous set of games"""")
            val nextLinkO = beforeLookup.afterGame.map(sg =>
              controllers.routes.GamesApi.directedGames(
                direction = LookupDirection.After,
                id = GameId(sg.id),
                queryCondition = queryCondition,
                limit = limit
              ).url).map(url => s"""<$url>; rel="next"; title="Next set of games"""")
            (beforeLookup.previous, previousLinkO.toList ++ nextLinkO)
          }
        }
        itemsSpecificLinksO match {
          case None => NotFound("Focus game not found")
          case Some((items, specificLinksL)) =>
            val links = qcLinks(queryCondition) ++ specificLinksL
            Ok(JsArray(items.map(_.enhancedNativeJson)))
              .withHeaders("Link" -> links.mkString(", "))
        }
      }
    }


  def timedGames(timing: TimingCondition, queryCondition: QueryCondition, limitCondition: LimitCondition) =
    Action.async { implicit req =>
      Async.async {
        Async.await(gamesService.loadGamesFromDatabase)

        val limit = limitCondition match {
          case DefaultLimit => 5
          case SpecificLimit(l) => l
        }

        val ws = WindowedSearch(filterGames(queryCondition)).SideBiasedLookup(limit)
        val (items, specificLinksO) = if (timing == First) {
          val fbl = ws.FirstBiasedLookup.apply()
          val fblLinks = fbl.laterFocus.map(sg =>
            controllers.routes.GamesApi.directedGames(
              direction = LookupDirection.After,
              id = GameId(sg.id),
              queryCondition = queryCondition,
              limit = limitCondition
            ).url).map(url => s"""<$url>; rel="next"; title="Next set of games"""")
          (fbl.items, fblLinks)
        } else {
          val lbl = ws.LastBiasedLookup.apply()
          val lblLinks = lbl.previousFocus.map(sg =>
            controllers.routes.GamesApi.directedGames(
              direction = LookupDirection.Before,
              id = GameId(sg.id),
              queryCondition = queryCondition,
              limit = limitCondition
            ).url).map(url => s"""<$url>; rel="previous"; title="Previous set of games"""")
          (lbl.items, lblLinks)
        }
        val links = qcLinks(queryCondition) ++ specificLinksO
        Ok(JsArray(items.map(_.enhancedNativeJson)))
          .withHeaders("Link" -> links.mkString(", "))
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

  def qcLinks(queryCondition: QueryCondition) = {
    List({
      val liveGames = controllers.routes.GamesApi.liveGames(queryCondition).url
      s"""<$liveGames>; rel="related"; title="Live games based on this query""""
    }, {
      val newGames = controllers.routes.GamesApi.newGames(queryCondition).url
      s"""<$newGames>; rel="related"; title="New games based on this query""""
    })
  }

  def filterGames(queryCondition: QueryCondition) =
    gamesService.gamesAgt.get().games.valuesIterator.filter(queryCondition).toVector.sortBy(_.id)

  def gameById(gameId: GameId, queryCondition: QueryCondition) = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)

      WindowedSearch(filterGames(queryCondition))
        .FocusedLookup(_.id == gameId.gameId)
        .SingleLookup.apply() match {
        case None => NotFound("Game not found.")
        case Some(result) =>
          val nextGameLinkO = result.next.map(sg =>
            controllers.routes.GamesApi.gameById(gameId = GameId(sg.id), queryCondition = queryCondition).url)
            .map(url => s"""<$url>; rel="next"; title="Next game"""")
          val previousGameLinkO = result.previous.map(sg =>
            controllers.routes.GamesApi.gameById(gameId = GameId(sg.id), queryCondition = queryCondition).url)
            .map(url => s"""<$url>; rel="previous"; title="Previous game"""")
          val links = qcLinks(queryCondition) ++ nextGameLinkO ++ previousGameLinkO
          Ok(result.focus.enhancedNativeJson)
            .withHeaders("Link" -> links.mkString(", "))
      }
    }
  }

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