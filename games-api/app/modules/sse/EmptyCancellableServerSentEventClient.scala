package modules.sse

import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Graph, SinkShape}
import de.heikoseeberger.akkasse.ServerSentEvent

import scala.concurrent.Promise

class EmptyCancellableServerSentEventClient(implicit actorMaterializer: ActorMaterializer) extends CancellableServerSentEventClient {
  override def createStream[T](sink: Graph[SinkShape[ServerSentEvent], T]): Promise[Unit] = {
    Source.empty.to(sink).run()
    Promise.successful(())
  }

  override def shutdown(): Unit = ()
}
