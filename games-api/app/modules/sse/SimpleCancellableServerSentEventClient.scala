package modules.sse

import java.net.URI

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Keep
import akka.stream.{ActorMaterializer, Graph, SinkShape}
import de.heikoseeberger.akkasse.ServerSentEvent

import scala.concurrent.{ExecutionContext, Promise}

class SimpleCancellableServerSentEventClient(uri: URI)
                                 (implicit
                                  actorSystem: ActorSystem,
                                  actorMaterializer: ActorMaterializer,
                                  executionContext: ExecutionContext) extends CancellableServerSentEventClient {

  val endStreamsAgent = Agent(Set.empty[Promise[Unit]])

  override def createStream[T](sink: Graph[SinkShape[ServerSentEvent], T]): Promise[Unit] = {

    val (_, finishStreamingPromise) = CancellableServerSentEventClient.createPermanentStream(
      Http().outgoingConnection(
        host = uri.getHost,
        port = if (uri.getPort == -1) 80 else uri.getPort
      ),
      HttpRequest(uri = uri.getPath)
    ).viaMat(StopperFlow())(Keep.both).to(sink).run()

    endStreamsAgent.alter(_ + finishStreamingPromise)

    finishStreamingPromise

  }

  override def shutdown(): Unit = endStreamsAgent.get().filterNot(_.isCompleted).foreach(_.success(()))

}
