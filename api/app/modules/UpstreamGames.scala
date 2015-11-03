package modules

import java.net.URI
import javax.inject._

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import de.heikoseeberger.akkasse.ServerSentEvent
import modules.UpstreamGames.Flush
import modules.sse.{CancellableServerSentEventClient, EmptyCancellableServerSentEventClient, SimpleCancellableServerSentEventClient}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait UpstreamGames {
  def allClient: CancellableServerSentEventClient
  def allAndNewClient: CancellableServerSentEventClient
  def newClient: CancellableServerSentEventClient
  def liveGames: CancellableServerSentEventClient
}

class UpstreamGamesNone @Inject()()(implicit actorSystem: ActorSystem) extends UpstreamGames {
  private implicit val actorMaterializer = ActorMaterializer()
  val allClient: CancellableServerSentEventClient = new EmptyCancellableServerSentEventClient()
  val allAndNewClient: CancellableServerSentEventClient = new EmptyCancellableServerSentEventClient()
  val newClient: CancellableServerSentEventClient = new EmptyCancellableServerSentEventClient()
  val liveGames: CancellableServerSentEventClient = new EmptyCancellableServerSentEventClient()
}

@Singleton
class UpstreamGamesLive @Inject()(configuration: Configuration, applicationLifecycle: ApplicationLifecycle)
                             (implicit actorSystem: ActorSystem,
                              executionContext: ExecutionContext) extends UpstreamGames {

  private implicit val actorMaterializer = ActorMaterializer()

  private val configPath = "gg.duel.pinger-service.url"

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

  val allClient: CancellableServerSentEventClient = buildStreamClient("/games/all/")
  val allAndNewClient: CancellableServerSentEventClient = buildStreamClient("/games/all-and-new/")
  val newClient: CancellableServerSentEventClient = buildStreamClient("/games/new/")
  val liveGames: CancellableServerSentEventClient = buildStreamClient("/games/live/")

  /**
   * Collect latest state for each game ID.
   * Flush it all every 5 seconds.
   */


  applicationLifecycle.addStopHook(() => Future.successful{
    allClient.shutdown()
    newClient.shutdown()
    allAndNewClient.shutdown()
    liveGames.shutdown()
  })

}
object UpstreamGames {

  case object Flush
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


  def sseFlushToEvents = Flow.apply[Either[ServerSentEvent, Flush.type]].scan(Map.empty[String, ServerSentEvent] -> List.empty[ServerSentEvent]){
    case ((map, _), Right(Flush)) =>
      Map.empty[String, ServerSentEvent] -> map.valuesIterator.toList
    case ((map, _), Left(event)) if event.id.isDefined =>
      map.updated(event.id.get, event) -> List.empty
    case (stuff, _) => stuff
  }.mapConcat{case (_, events) => events}


}

