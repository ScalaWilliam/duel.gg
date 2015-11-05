import com.google.inject.AbstractModule
import gg.duel.WindowedSearch
import gg.duel.query._
import modules.GamesService
import play.api.inject.guice.{GuiceableModule, GuiceableModuleConversions, GuiceApplicationLoader}
import play.api.mvc.Controller
import play.api.routing.Router
import play.api.routing.Router.Routes

import scala.async.Async
import scala.concurrent.{Future, ExecutionContext}


class MyApplicationLoader extends GuiceApplicationLoader with GuiceableModuleConversions {
  override protected def overrides(context: play.api.ApplicationLoader.Context): Seq[GuiceableModule] = {
    Seq(fromGuiceModule(new RouterModule)) ++ super.overrides(context)
  }
}
class RouterModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Router]).to(classOf[K])
  }
}

import javax.inject._
class K @Inject()(gamesService: GamesService)
                 (implicit executionContext: ExecutionContext) extends Router with Controller {


  import play.api.mvc._
  import play.api.routing._
  import play.api.routing.sird._


  def ReadyAction: ActionBuilder[Request] =
    new ActionBuilder[Request] with Results {
      override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
        Async.async {
          Async.await(gamesService.loadGamesFromDatabase)
          Async.await(block(request))
        }
      }
    }

  case class QueryConditionContext(queryCondition: QueryCondition) {

    def filterGames = WindowedSearch(gamesService.gamesAgt.get().games.valuesIterator.toVector.sortBy(_.id))

    case class TimingContext(timingCondition: TimingCondition) {

    }

    case class FocusedContext(gameId: GameId) {
      def focusedLookup = filterGames.FocusedLookup.apply(_.id == gameId.gameId)
      def processOpt[T](f: => Option[T])(g: T => Result): Result =
        f.map(g).getOrElse(NotFound(s"Game $gameId not found"))
      case class DirectedContext(direction: LookupDirection, limit: LimitCondition) {
        def multipleLookup = focusedLookup.MultipleLookup(limit = 5)
        def getGames = ReadyAction { request =>
          if ( direction.isAfter ) {
            processOpt(multipleLookup.AfterLookup.apply()) { game =>
              Ok(game.focus)
            }
          } else {
            processOpt(multipleLookup.BeforeLookup.apply()) { game =>
              Ok(game.focus)
            }
          }
        }
      }
      def getGame = ReadyAction { request =>
        focusedLookup.SingleLookup.apply() match {
          case None => NotFound("Game not found")
          case Some(game) => Ok(game.focus.enhancedNativeJson)
        }
      }
    }
  }

  import binders._
  object qc {
    def unapply(requestHeader: RequestHeader): Option[QueryCondition] = {
      QueryCondition.apply(requestHeader.queryString).right.toOption
    }
  }

  object gt {
    def unapply(requestHeader: RequestHeader): Option[GameType] = {
      GameType.apply(requestHeader.queryString).right.toOption
    }
  }

//  def directedGames(direction: LookupDirection, id: GameId, queryCondition: QueryCondition, limit: LimitCondition) =
//  def timedGames(timing: TimingCondition, queryCondition: QueryCondition, limitCondition: LimitCondition) =
//  def gameById(gameId: GameId, queryCondition: QueryCondition) = Action.async {

  val router = Router.from {
    case GET(p"/hello/$to" & qc(queryCondition)) =>
      QueryConditionContext(queryCondition).ha
  }

  override def routes: Routes = router.routes

  override def withPrefix(prefix: String): Router = router.withPrefix(prefix)

  override def documentation: Seq[(String, String, String)] = router.documentation
}