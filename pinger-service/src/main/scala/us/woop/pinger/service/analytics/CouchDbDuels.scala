package us.woop.pinger.service.analytics
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import spray.http.HttpResponse
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.service.analytics.CouchDbDuels.{RequestResponse, CouchDbPath}
import scala.concurrent.{ExecutionContext, Future}

object CouchDbDuels {
  case class CouchDbPath(contextPath: String)
  case class RequestResponse(url: String, body: String, response: HttpResponse)
}
class CouchDbDuels(couchDbPath: CouchDbPath) extends Act with ActorLogging{

  import spray.http._
  import spray.client.pipelining._
  import ExecutionContext.Implicits.global
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  become {
    case completedDuel: CompletedDuel =>
      val simpleDuel = completedDuel.toSimpleCompletedDuel
      val jsonString = simpleDuel.toPrettyJson
      import akka.pattern.pipe
      val url = s"${couchDbPath.contextPath}/${simpleDuel.simpleId}"
      val request = Put(url, jsonString)
      (for {
        response <- pipeline(request)
      } yield RequestResponse(url, jsonString, response)) pipeTo self
    case rr @ RequestResponse(_, _, response) if response.status.isFailure =>
      log.info("Couch responded with failure: {}", rr)
  }
}
