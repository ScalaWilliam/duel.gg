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


/**
package us.woop.pinger.actors

import akka.actor.ActorDSL._
import akka.actor._
import us.woop.pinger.data.actor.{ParsedSubscriber, PingPongProcessor}
import PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import akka.actor.TypedActor.TypedActor
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import akka.actor.Terminated
import akka.actor.Actor.Receive

class PublishParsedMessagesActor extends SubscribesToServers with akka.actor.TypedActor.Receiver {
  def context = TypedActor.context
  val subscriptions = collection.mutable.Set[(Server, ActorRef)]()
  import ParsedSubscriber._
  def onReceive(message: Any, sender: ActorRef) = {
    message match {
      case m @ ParsedMessage(server, _, value) =>
        for { (`server`, target) <- subscriptions } target ! m
      case Terminated(client) =>
        TypedActor.context unwatch client
        for { subscription @ (server, `client`) <- subscriptions }
          subscriptions remove subscription
    }
  }
}

trait SubscribesToServers {
  protected def context: ActorContext
  def subscriptions: collection.mutable.Set[(Server, ActorRef)]
  def handleTerminationBehaviour: Receive = {
    case Terminated(client) if subscriptions.exists{_._2 == client} =>
      context unwatch client
      for { subscription @ (server, `client`) <- subscriptions }
        subscriptions remove subscription
  }
  def Subscribe(server: Server) {
    val sender = context.sender()
    val subscription = server -> sender
    subscriptions += subscription
    context watch sender
  }
  def Unsubscribe(server: Server) {
    val sender = context.sender()
    val subscription = server -> sender
    subscriptions -= subscription
    context unwatch sender
  }
}

trait SubscribesToFirehose {
  def listeners: collection.mutable.Set[ActorRef]
  def context: ActorContext
  def Listen() {
    val sender = context.sender()
    listeners += sender
    context watch sender
  }
  def Unlisten() {
    val sender = context.sender()
    listeners -= sender
    context unwatch sender
  }
  def handleTerminationBehaviour: Receive = {
    case Terminated(client) if listeners contains client =>
      listeners -= client
      context unwatch client
  }
}

  **/