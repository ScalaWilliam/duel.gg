package modules

import java.net.URI
import javax.inject._

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import de.heikoseeberger.akkasse.ServerSentEvent
import modules.sse.{CancellableServerSentEventClient, EmptyCancellableServerSentEventClient, SimpleCancellableServerSentEventClient}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class UpstreamGames @Inject()(configuration: Configuration, applicationLifecycle: ApplicationLifecycle)
                             (implicit actorSystem: ActorSystem,
                              executionContext: ExecutionContext) {

  implicit val actorMaterializer = ActorMaterializer()

  val configPath = "gg.duel.pinger-service.url"

  private def buildStreamClient(endpointName: String): CancellableServerSentEventClient = {
    configuration.getString(configPath)
      .flatMap { uri => Try(new URI(s"$uri$endpointName")).toOption } match {
      case Some(uri) =>
        Logger.info(s"Using uri $uri")
        new SimpleCancellableServerSentEventClient(uri)
      case None =>
        Logger.error(s"Configuration setting at path '$configPath' invalid: ${configuration.getString(configPath)}")
        new EmptyCancellableServerSentEventClient()
    }
  }

  val allClient = buildStreamClient("/games/all/")
  val allAndNewClient = buildStreamClient("/games/all-and-new/")
  val newClient = buildStreamClient("/games/new/")
  val liveGames = buildStreamClient("/games/live/")

  /**
   * Collect latest state for each game ID.
   * Flush it all every 5 seconds.
   */

  case object Flush

  def sseFlushToEvents = Flow.apply[Either[ServerSentEvent, Flush.type]].scan(Map.empty[String, ServerSentEvent] -> List.empty[ServerSentEvent]){
    case ((map, _), Right(Flush)) =>
      Map.empty[String, ServerSentEvent] -> map.valuesIterator.toList
    case ((map, _), Left(event)) if event.id.isDefined =>
      map.updated(event.id.get, event) -> List.empty
    case (stuff, _) => stuff
  }.mapConcat{case (_, events) => events}

  import concurrent.duration._
  def flw = {
    Flow() {implicit builder =>
      import FlowGraph.Implicits._
      val flusher = Source.apply(5.seconds, 5.seconds, Right(Flush))
      val sem = builder.add(Flow.apply[ServerSentEvent].map(Left.apply))
      val merge = builder.add(Merge.apply[Either[ServerSentEvent, Flush.type]](2))
      sem ~> merge.in(0)
      flusher ~> merge.in(1)
      (sem.inlet, (merge ~> sseFlushToEvents).outlet)
    }
  }


  applicationLifecycle.addStopHook(() => Future.successful{
    allClient.shutdown()
    newClient.shutdown()
    allAndNewClient.shutdown()
    liveGames.shutdown()
  })

}

