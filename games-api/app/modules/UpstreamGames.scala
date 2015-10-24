package modules

import java.net.URI
import javax.inject._

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import modules.sse.{EmptyCancellableServerSentEventClient, SimpleCancellableServerSentEventClient}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class UpstreamGames @Inject()(configuration: Configuration, applicationLifecycle: ApplicationLifecycle)
                             (implicit actorSystem: ActorSystem,
                              executionContext: ExecutionContext) {

  implicit val actorMaterializer = ActorMaterializer()

  val configPath = "gg.duel.pinger-service.all-and-new-url"

  val client = configuration.getString(configPath)
    .flatMap { uri => Try(new URI(uri)).toOption } match {
    case Some(uri) =>
      Logger.info(s"Using uri $uri")
      new SimpleCancellableServerSentEventClient(uri)
    case None =>
      Logger.error(s"Configuration setting at path '$configPath' invalid: ${configuration.getString(configPath)}")
      new EmptyCancellableServerSentEventClient()
  }

  client.createStream(Sink.foreach(println))

  applicationLifecycle.addStopHook(() => Future.successful(client.shutdown()))

}

