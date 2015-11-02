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
                 serverFilter: ServerFilter) = Action.async { implicit request =>
    val liveGamesUrl = controllers.routes.GamesApi.liveGames(gameType, playerCondition, tagFilter, serverFilter).url
    val newGamesUrl = controllers.routes.GamesApi.newGames(gameType, playerCondition, tagFilter, serverFilter).url
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
          val (returnJason, previousUrlO, nextUrlO) = games.splitAt(index) match {
            case (previousGames, currentGame :: nextGames) =>

              focus match {
                case SimpleFocus =>
                  val simpleFocusResult = SimpleFocus.collect(
                    previous = previousGames.reverse.toVector,
                    focus = currentGame,
                    next = nextGames.toVector
                  )
                  // link to the game we've not yet seen, effectively forming a pagination
                  val nextPageUrlO = nextGames.drop(1).headOption.map(nid =>  controllers.routes.GamesApi.focusGames(
                    AsymmetricFocus(previous = 0, next = 2), gameType, GameId(nid.id), playerCondition, tagFilter, serverFilter).url)
                  val previousPageUrlO = previousGames.dropRight(1).lastOption.map(nid =>controllers.routes.GamesApi.focusGames(
                    AsymmetricFocus(previous = 2, next = 0), gameType, GameId(nid.id), playerCondition, tagFilter,
                    serverFilter).url)
                  (Json.toJson(simpleFocusResult.map(_.enhancedNativeJson)), previousPageUrlO, nextPageUrlO)
                case mf: MultipleFocus =>
                  val multipleFocusResult = mf.collect(
                    previous = previousGames.reverse.toVector,
                    focus = currentGame,
                    next = nextGames.toVector
                  )
                  val nextPageUrlO = multipleFocusResult.next.flatMap(n => nextGames.drop(n.size).headOption.map(nid => controllers.routes.GamesApi.focusGames(
                    mf match {
                      case RadialFocus(r) => AsymmetricFocus(previous = 0, next = r)
                      case AsymmetricFocus(p, nx) => AsymmetricFocus(previous = 0, next = Math.max(p, nx))
                    }, gameType, GameId(nid.id), playerCondition, tagFilter,
                    serverFilter).url))
                  val previousPageUrlO = multipleFocusResult.previous.flatMap(n => previousGames.dropRight(n.size).lastOption.map(nid => controllers.routes.GamesApi.focusGames(
                    mf match {
                      case RadialFocus(r) => AsymmetricFocus(previous = r, next = 0)
                      case AsymmetricFocus(p, nx) => AsymmetricFocus(previous = Math.max(p, nx), next = 0)
                    }, gameType, GameId(nid.id), playerCondition, tagFilter,
                    serverFilter).url))
                  (Json.toJson(multipleFocusResult.map(_.enhancedNativeJson)), previousPageUrlO, nextPageUrlO)
              }
          }
          val links = List(s"""<$liveGamesUrl>; rel="related"; title="Live game updates SSE stream"""",
            s"""<$newGamesUrl>; rel="related"; title="New games SSE stream"""") ++
            nextUrlO.map(pu => s"""<$pu>; rel=next" title="Next focused games"""") ++
            previousUrlO.map(pu => s"""<$pu>; rel="previous"; title="Previous focused games"""")
          Ok(returnJason).withHeaders("Link" -> links.mkString(", "))
      }
    }
  }


  def timedGames(gameType: GameType, timing: TimingCondition, playerCondition: PlayerCondition,
                 limitCondition: LimitCondition, tagFilter: TagFilter, serverFilter: ServerFilter) = Action.async { implicit req =>
    Async.async {
      Async.await(gamesService.loadGamesFromDatabase)
      val liveGamesUrl = controllers.routes.GamesApi.liveGames(gameType, playerCondition, tagFilter, serverFilter).url
      val newGamesUrl = controllers.routes.GamesApi.newGames(gameType, playerCondition, tagFilter, serverFilter).url
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
      val links = List(
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
      content = gamesService.newGamesEnum.flatMap {
        case (sg, evt) if gameType(sg) && playerCondition(sg) && tagFilter(sg) && serverFilter(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }

  def liveGames(gameType: GameType, playerCondition: PlayerCondition,
                tagFilter: TagFilter, serverFilter: ServerFilter) = Action {
    Ok.feed(
      content = gamesService.liveGamesEnum.flatMap {
        case (sg, evt) if gameType(sg) && playerCondition(sg) && tagFilter(sg) && serverFilter(sg) =>
          Enumerator(evt)
        case _ => Enumerator.empty[play.api.libs.EventSource.Event]
      }
    ).as("text/event-stream")
  }

}