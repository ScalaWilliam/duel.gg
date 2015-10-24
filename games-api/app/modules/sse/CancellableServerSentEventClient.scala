package modules.sse

import java.net.URI

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{FlattenStrategy, Source}
import akka.stream.stage.{TerminationDirective, SyncDirective, Context, PushStage}
import akka.stream.{ActorMaterializer, Graph, SinkShape}
import de.heikoseeberger.akkasse.{EventStreamUnmarshalling, ServerSentEvent}

import scala.concurrent.{ExecutionContext, Promise}

trait CancellableServerSentEventClient {
  def createStream[T](sink: Graph[SinkShape[ServerSentEvent], T]): Promise[Unit]

  def shutdown(): Unit
}

object CancellableServerSentEventClient {
  
  def createPermanentStream(httpConnection: HttpConnection, httpRequest: HttpRequest)
                        (implicit actorMaterializer: ActorMaterializer,
                         executionContext: ExecutionContext): Source[ServerSentEvent, Unit] = {
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
