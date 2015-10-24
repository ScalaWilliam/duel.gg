package modules

import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FlattenStrategy, Flow, Source}
import akka.stream.stage.{Context, PushStage, SyncDirective, TerminationDirective}
import de.heikoseeberger.akkasse.ServerSentEvent

import scala.concurrent.{ExecutionContext, Future}

object UpstreamGames {

  type HttpConnection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]

  def getPermanentStream(httpConnection: HttpConnection)(httpRequest: HttpRequest)(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext): Source[ServerSentEvent, Unit] = {
    import de.heikoseeberger.akkasse.EventStreamUnmarshalling._
    Source.repeat {
      Source.single(httpRequest)
        .via(httpConnection)
        .transform(() => finishOnFailure)
        .mapAsync(1)(Unmarshal(_).to[Source[ServerSentEvent, Any]])
        .flatten(FlattenStrategy.concat)
        .transform(() => finishOnFailure)
    }.flatten(FlattenStrategy.concat)
  }

  def finishOnFailure[T] = new PushStage[T, T] {
    override def onPush(element: T, ctx: Context[T]): SyncDirective = {
      ctx.push(element)
    }

    override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
      ctx.finish()
    }
  }


}