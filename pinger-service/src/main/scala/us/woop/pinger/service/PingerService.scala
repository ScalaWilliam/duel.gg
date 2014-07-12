package us.woop.pinger.service

import akka.actor.ActorSystem
import us.woop.pinger.MyId
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.service.PingerController.{Monitor, Unmonitor}
import us.woop.pinger.service.individual.ServerMonitor.ServerStateChanged
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor.S3Access

object PingerService extends App {

  implicit val as = ActorSystem("dudeWat")

  import akka.actor.ActorDSL._
  val root = actor(factory = as, name = "controller")(new Act {

    val hazelcast = context.actorOf(
      props = HazelcastBridgeActor.props,
      name = "hazelcast"
    )

    val pingerController = as.actorOf(
      props = PingerController.props,
      name = "pingerController"
    )

    val rawMessagePublished = as.actorOf(
      props = PublishRawMessagesToS3Actor.props(
        S3Access(MyId.default), 100
      )
    )

    become {
      case p: ParsedMessage =>
        rawMessagePublished ! p
        hazelcast ! p
      case s: ServerStateChanged =>
        hazelcast ! s
      case m: Monitor =>
        pingerController ! m
      case u: Unmonitor =>
        pingerController ! u
    }

  })


}
