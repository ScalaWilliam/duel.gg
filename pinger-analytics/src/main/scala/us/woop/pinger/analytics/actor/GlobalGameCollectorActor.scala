package us.woop.pinger.analytics.actor

import akka.actor.ActorDSL._
import akka.actor.{ActorRef, ActorLogging}
import us.woop.pinger.client.PingPongProcessor
import PingPongProcessor.Server
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor
import IndividualGameCollectorActor.HaveGame
import us.woop.pinger.data.ParsedPongs.ParsedMessage

class GlobalGameCollectorActor extends Act with ActorLogging {

  val serverProcessors = collection.mutable.HashMap[Server, ActorRef]()

  whenStarting {
    log.info("Global game collector started")
  }
  def makeServerActor(forServer: Server) =
    actor(context, name = s"${forServer.ip.ip}:${forServer.port}")(new IndividualGameCollectorActor(forServer))

  become {
    case parsedMessage@ParsedMessage(server, time, message) =>
      serverProcessors.getOrElseUpdate(server, makeServerActor(server)) ! parsedMessage
    case game: HaveGame =>
      log.info("Global game collector received a new game for {}", game.server)
      context.parent ! game
  }

}
