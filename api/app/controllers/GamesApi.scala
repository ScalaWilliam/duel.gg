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
      val liveGamesUrl = controllers.routes.GamesApi.liveGames(queryCondition).url
      val newGamesUrl = controllers.routes.GamesApi.newGames(queryCondition).url
      val xlinks = List(
        s"""<$liveGamesUrl>; rel="related"; title="Live game updates SSE stream"""",
        s"""<$newGamesUrl>; rel="related"; title="New games SSE stream""""
      )
      Async.async {
        Async.await(gamesService.loadGamesFromDatabase)
        val games = gamesService.gamesAgt.get().games.valuesIterator.filter(queryCondition).toList.sortBy(_.id)
        games.indexWhere(_.id == id.gameId) match {
          case -1 => NotFound("Focus game not found")
          case index =>
            games.splitAt(index) match {
              case (previousGames, focusGame :: nextGames) =>
                val n = limit match {
                  case DefaultLimit => 5
                  case SpecificLimit(l) => l
                }
                def fullLink(gameId: GameId, direction: LookupDirection, title: String) = {
                  val url = controllers.routes.GamesApi.directedGames(
                    direction = direction, id = gameId, queryCondition = queryCondition, limit = limit
                  )
                  s"""<$url>; rel="${direction.httpValue}"; title="$title""""
                }
                if (direction.isAfter) {
                  val nextLinkO = nextGames.take(n).lastOption.map(sg =>
                    fullLink(gameId = GameId(sg.id), direction = LookupDirection.After, title = "More recent set of games"))
                  val previousLink = fullLink(gameId = id, direction = LookupDirection.Before, title = "Games before this one")
                  val json = JsArray(nextGames.take(n).map(_.enhancedNativeJson))
                  val links = xlinks ++ nextLinkO ++ List(previousLink)
                  Ok(json).withHeaders("Link" -> links.mkString(", "))
                } else {
                  val nextLink = fullLink(gameId = id, direction = LookupDirection.After, title = "Games after this one")
                  val previousLinkO = if ( previousGames.size > n ) previousGames.takeRight(n).headOption.map(sg =>
                    fullLink(gameId = GameId(sg.id), direction =direction, title = "More recent set of games"))
                  else Option.empty
                  val json = JsArray(previousGames.takeRight(n).map(_.enhancedNativeJson).reverse)
                  val links = List(nextLink) ++ previousLinkO ++ xlinks
                  Ok(json).withHeaders("Link" -> links.mkString(", "))
                }
            }
        }
      }
    }


  def timedGames(timing: TimingCondition, queryCondition: QueryCondition, limitCondition: LimitCondition) =
    Action.async { implicit req =>
      Async.async {
        Async.await(gamesService.loadGamesFromDatabase)
        val liveGamesUrl = controllers.routes.GamesApi.liveGames(queryCondition).url
        val newGamesUrl = controllers.routes.GamesApi.newGames(queryCondition).url
        var games = gamesService.gamesAgt.get().games.valuesIterator
          .filter(queryCondition)
          .toList.sortBy(_.id)

        if (timing == Recent) games = games.reverse

        val limit = limitCondition match {
          case DefaultLimit => 5
          case SpecificLimit(l) => l
        }

        val gl = games.take(limit)
        val backLinkO = if ( games.size > limit ) gl.lastOption.map { focusGame =>
          val direction = if ( timing.isFirst ) LookupDirection.After else LookupDirection.Before
          val title = if ( timing.isFirst ) "Next games" else "Previous games"
          val url = controllers.routes.GamesApi.directedGames(
            direction = direction,
            id = GameId(focusGame.id),
            queryCondition = queryCondition,
            limit = limitCondition
          )
          s"""<$url>; rel="${direction.httpValue}"; title="$title""""
        } else Option.empty

        val links = backLinkO.toList ++ List(
          s"""<$liveGamesUrl>; rel="related"; title="Live game updates SSE stream"""",
          s"""<$newGamesUrl>; rel="related"; title="New games SSE stream""""
        )
        Ok(JsArray(gl.map(g => Json.toJson(g.enhancedNativeJson)))).withHeaders("Link" -> links.mkString(", "))
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

  def gameById(gameId: GameId, queryCondition: QueryCondition) = Action.async {
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      val games = gamesService.gamesAgt.get().games.valuesIterator.filter(queryCondition).toList.sortBy(_.id)
      games.indexWhere(_.id == gameId.gameId) match {
        case -1 => NotFound("Game not found.")
        case index =>
          games.splitAt(index) match {
            case (previousGames, focusGame :: nextGames) =>
              val liveGamesUrl = controllers.routes.GamesApi.liveGames(queryCondition).url
              val newGamesUrl = controllers.routes.GamesApi.newGames(queryCondition).url
              val nextLinkO = nextGames.headOption.map(sg =>
                controllers.routes.GamesApi.gameById(gameId = GameId(sg.id), queryCondition = queryCondition).url)
              .map(url => s"""<$url>; rel="next"; title="Next game"""")
              val previousLinkO = previousGames.lastOption.map(sg =>
                controllers.routes.GamesApi.gameById(gameId = GameId(sg.id), queryCondition = queryCondition).url)
              .map(url => s"""<$url>; rel="previous"; title="Previous game"""")

              val links = nextLinkO.toList ++ previousLinkO.toList ++ List(
                s"""<$liveGamesUrl>; rel="related"; title="Live game updates SSE stream"""",
                s"""<$newGamesUrl>; rel="related"; title="New games SSE stream""""
              )
              Ok(focusGame.enhancedNativeJson).withHeaders("Link" -> links.mkString(", "))
          }
      }
    }
  }

  def newGames(queryCondition: QueryCondition) = Action {
    Ok.feed(
      content = gamesService.newGamesEnum.flatMap {
        case (sg, evt) if queryCondition(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }

  def liveGames(queryCondition: QueryCondition) = Action {
    Ok.feed(
      content = gamesService.liveGamesEnum.flatMap {
        case (sg, evt) if queryCondition(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }

}