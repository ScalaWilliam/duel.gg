import java.time.ZonedDateTime
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import services.AuthenticationService
import scala.concurrent.Future

/**
 * Created on 27/08/2015.
 */
package object controllers {

  def WriteCheckAction(implicit authenticationService: AuthenticationService): ActionBuilder[Request] =
    new ActionBuilder[Request] with Results {
      override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
        request.getQueryString("api-key") match {
          case Some(apiKey) if authenticationService.authenticates(apiKey) => block(request)
          case _ => Future.successful(Unauthorized("A valid API key is required."))
        }
      }
    }

  def now(): ZonedDateTime = ZonedDateTime.now()

}
