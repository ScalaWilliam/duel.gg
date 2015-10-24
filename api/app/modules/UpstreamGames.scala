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

  /*
  allClient.createStream{
    Flow.apply[ServerSentEvent]
      .fold(Option.empty[ServerSentEvent]){
        (o, e) =>
          if ( o.isEmpty ) println("Started: ", System.currentTimeMillis() / 1000)
          Option(e)
      }.to(Sink.foreach(x => println(s"DONE: $x", System.currentTimeMillis() / 1000)))
  }
  */

  applicationLifecycle.addStopHook(() => Future.successful{
    allClient.shutdown()
    newClient.shutdown()
    allAndNewClient.shutdown()
  })

}

