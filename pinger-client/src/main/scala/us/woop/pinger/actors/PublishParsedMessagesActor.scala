package us.woop.pinger.actors

import akka.actor.ActorDSL._
import akka.actor.{Terminated, ActorRef}
import us.woop.pinger.data.actor.{ParsedSubscriber, PingPongProcessor}
import PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage


class PublishParsedMessagesActor extends Act {
  val subscriptions = collection.mutable.Set[(Server, ActorRef)]()
  import ParsedSubscriber._

  become {

    case m @ ParsedMessage(server, _, value) =>
      for { (`server`, target) <- subscriptions } target ! m

    case Subscribe(server) =>
      subscriptions += server -> sender()
      context watch sender()

    case Unsubscribe(server) =>
      subscriptions -= server -> sender()
      context unwatch sender()

    case Terminated(client) =>
      context unwatch client
      for { item @ (server, `client`) <- subscriptions }
        subscriptions remove item


  }
}
