package modules

import javax.inject._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{Attributes, FanInShape, ActorMaterializer}
import akka.stream.FanInShape.{Name, Init}
import akka.stream.scaladsl._
import akka.stream.stage.{Context, PushStage, SyncDirective, TerminationDirective}
import de.heikoseeberger.akkasse.{EventStreamUnmarshalling, ServerSentEvent}
import modules.UpstreamGames.StopperFlow
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Promise, Future}



@Singleton
class UpstreamGames @Inject()(configuration: Configuration, applicationLifecycle: ApplicationLifecycle)
                             (implicit actorSystem: ActorSystem,
                              executionContext: ExecutionContext) {

  implicit val actorMaterializer = ActorMaterializer()

  val sinkIt = Sink.foreach(println)

  val (_, endIt) = UpstreamGames.getPermanentStream(
    Http().outgoingConnection(host = "localhost", port = 9000),
    HttpRequest(uri = "/games/all-and-new/")
  ).viaMat(StopperFlow())(Keep.both).to(sinkIt).run()

  applicationLifecycle.addStopHook(() => Future.successful(endIt.success(())))

}

object UpstreamGames {

  /** Courtesy of http://stackoverflow.com/a/31425051/2789308 **/
  object StopperFlow {

    private class StopperMergeShape[A](_init: Init[A] = Name("StopperFlow")) extends FanInShape[A](_init) {
      val in = newInlet[A]("in")
      val stopper = newInlet[Unit]("stopper")

      override protected def construct(init: Init[A]): FanInShape[A] = new StopperMergeShape[A](init)
    }

    private class StopperMerge[In] extends FlexiMerge[In, StopperMergeShape[In]](
      new StopperMergeShape(), Attributes.name("StopperMerge")) {
      import FlexiMerge._

      override def createMergeLogic(p: PortT) = new MergeLogic[In] {
        override def initialState =
          State[In](Read(p.in)) { (ctx, input, element) =>
          ctx.emit(element)
          SameState
        }

        override def initialCompletionHandling = eagerClose
      }
    }

    def apply[In](): Flow[In, In, Promise[Unit]] = {
      val stopperSource = Source.lazyEmpty[Unit]
      import akka.stream.scaladsl._
      import FlowGraph.Implicits._
      Flow(stopperSource) { implicit builder =>
        stopper =>
          val stopperMerge = builder.add(new StopperMerge[In]())

          stopper ~> stopperMerge.stopper

          (stopperMerge.in, stopperMerge.out)
      }

    }
  }

  type HttpConnection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]

  def getPermanentStream(httpConnection: HttpConnection, httpRequest: HttpRequest)
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