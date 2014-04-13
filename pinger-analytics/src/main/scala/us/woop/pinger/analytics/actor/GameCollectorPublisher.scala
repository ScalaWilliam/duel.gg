package us.woop.pinger.analytics.actor

import akka.actor.ActorDSL._
import akka.actor.{Terminated, ActorRef, ActorLogging}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor
import IndividualGameCollectorActor.HaveGame
import us.woop.pinger.analytics.actor.data.GameCollectorPublisher.{Unlisten, Listen}


class GameCollectorPublisher extends Act with ActorLogging {
  val gameCollector = actor(context, name = "gameCollector")(new GlobalGameCollectorActor)
  val subscribers = collection.mutable.Set[ActorRef]()
  become {
    case Listen if !(subscribers contains sender()) =>
      subscribers += sender()
      context watch sender()
    case Unlisten if subscribers contains sender() =>
      subscribers -= sender()
      context unwatch sender()
    case Terminated(listener) if subscribers contains listener =>
      subscribers -= listener
      context unwatch listener
    case m: ParsedMessage =>
      gameCollector ! m
    case game: HaveGame =>
      for {client <- subscribers} client ! game
  }
}
