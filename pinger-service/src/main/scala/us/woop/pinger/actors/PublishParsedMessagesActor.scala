package us.woop.pinger.actors

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, Terminated, ActorRef}
import us.woop.pinger.data.actor.{ParsedSubscriber, PingPongProcessor}
import PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage


class PublishParsedMessagesActor extends Act with ActorLogging {

  val subscriptions = collection.mutable.Set[(Server, ActorRef)]()

  import ParsedSubscriber._
  whenStarting{
    log.info("Starting parsed message publisher actor...")
  }
  become {

    case m @ ParsedMessage(server, _, value) =>
      for { (`server`, target) <- subscriptions } target ! m

    case Subscribe(server) =>
      val subscription = server -> sender()
      subscriptions += subscription
      context watch sender()
      log.info("Parsed publisher subscription added: {}", subscription)

    case Unsubscribe(server) =>
      val subscription = server -> sender()
      subscriptions -= subscription
      context unwatch sender()
      log.info("Parsed publisher subscription removed: {}", subscription)

    case Terminated(client) =>
      context unwatch client
      log.info("Parsed publisher client died: {}", client)

      for { subscription @ (server, `client`) <- subscriptions }
        log.info("Parsed publisher client died - removing subscription {}", subscription)

  }
}
