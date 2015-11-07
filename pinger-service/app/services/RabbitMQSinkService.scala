package services

import javax.inject._

import actors.ForwardMessages
import akka.actor.{Kill, ActorSystem}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.google.common.net.MediaType
import gg.duel.pinger.mulprocess.Event
import io.scalac.amqp.{Routed, Message, Connection}
import akka.stream.scaladsl._
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 07/11/2015.
  */
@Singleton
class RabbitMQSinkService @Inject()(configuration: Configuration,
                                    pingerService: PingerService,
                                    applicationLifecycle: ApplicationLifecycle)
                                   (implicit actorSystem: ActorSystem, executionContext: ExecutionContext) {
  Logger.info("Starting RabbitMQ Sink service.")
  implicit private val am = ActorMaterializer()
  val connection = Connection(configuration.underlying)
  val queue = connection.consume(queue = "invoices")
  val exchange = connection.publish(exchange = "gg.updates")
  val evtToMessage = Flow[Event].mapConcat { evt =>
    evt.name.map { name =>
      Routed(
        routingKey = name,
        message =
          Message(
            body = ByteString(evt.data.getBytes("UTF-8")),
            contentType = Option(MediaType.JSON_UTF_8),
            headers = evt.id.map(gid => "game-id" -> gid).toMap
          )
      )
    }.toList
  }

  val haAct = Source.actorPublisher(ForwardMessages.props[Event])
    .via(evtToMessage).to(Sink(exchange)).run()

  applicationLifecycle.addStopHook(() => Future.successful {
    haAct ! Kill
  })
}
