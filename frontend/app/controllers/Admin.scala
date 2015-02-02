package controllers

import play.api.libs.json.Json
import play.api.mvc._
import plugins.RegisteredUserManager._
import plugins._
import scala.concurrent.{ExecutionContext, Promise, Future}
import scala.async.Async.{async, await}
import scala.util.{Failure, Success, Try}

object Admin extends Controller {

  def futureToTryFuture[T](f: Future[T])(implicit ec: ExecutionContext): Future[Try[T]] = {
    val p = Promise.apply[Try[T]]()
    f.onComplete{case r => p.complete(Try(r))}
    p.future
  }


  val SESSION_ID = "sessionId"
  import scala.concurrent.ExecutionContext.Implicits.global

  def stated[V](f: Request[AnyContent] => SessionState => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      RegisteredUserManager.userManagement.getSessionState.flatMap { implicit suzzy =>
        f(request)(suzzy)
      }
  }

  def statedSync[V](f: Request[AnyContent] => SessionState => Result): Action[AnyContent] =
    stated { a => b =>
      Future{f(a)(b)}
    }

  def registeredSync[V](f: Request[AnyContent] => RegisteredSession => Result): Action[AnyContent] =
    registered { a => b =>
      Future{f(a)(b)}
    }

  def registered[V](f: Request[AnyContent] => RegisteredSession => Future[Result]): Action[AnyContent] =
    stated { implicit request => {
      case SessionState(Some(sessionId), Some(GoogleEmailAddress(email)), Some(profile)) =>
        f(request)(RegisteredSession(sessionId, profile))
      case other =>
        Future {
          SeeOther(controllers.routes.Duelgg.createProfile().url)
        }
    }
  }

  def forceProfile(userId: String) = drakas {
    _ =>
      async{
        await(futureToTryFuture(DuelStoragePlugin.plugin.createProfile(userId))) match {
          case Success(_) =>
            DuelStoragePlugin.plugin.currentStorage.usersList.get(userId) match {
              case Some(user) =>
                Ok(Json.obj("user" -> user.asBasicJson))
              case None =>
                Ok(Json.obj("error" -> s"User '$userId' could not be generated."))
            }
          case Failure(e) =>
            Ok(Json.obj("error" -> s"Failed to generate user '$userId' due to $e"))
        }
      }
  }

  def drakas[V](f: Request[AnyContent] => Future[Result]): Action[AnyContent] =
  registered { request => {
    case rs@RegisteredSession(_, RegisteredUser("drakas", _, _, _)) =>
      f(request)
    case other =>
      Future {
        Unauthorized("You're not drakas.")
      }
  }
  }

  def index = drakas {
    _ =>
      Future { Ok("Yay")}
  }

}