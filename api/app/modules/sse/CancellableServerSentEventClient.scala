package modules.sse

import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, FlattenStrategy, Source}
import akka.stream.stage.{TerminationDirective, SyncDirective, Context, PushStage}
import akka.stream.{ActorMaterializer, Graph, SinkShape}
import de.heikoseeberger.akkasse.{EventStreamUnmarshalling, ServerSentEvent}
import play.api.Logger

import scala.concurrent.{Future, ExecutionContext, Promise}

trait CancellableServerSentEventClient {
  def createStream[T](sink: Graph[SinkShape[ServerSentEvent], T]): Promise[Unit]

  def shutdown(): Unit
}

object CancellableServerSentEventClient {

  type HttpConnection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]

  def createPermanentStream(httpConnection: HttpConnection, httpRequest: HttpRequest)
                        (implicit actorMaterializer: ActorMaterializer,
                         executionContext: ExecutionContext): Source[ServerSentEvent, Unit] = {
    Logger.info("Creating permanent stream...")
    import EventStreamUnmarshalling._
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
