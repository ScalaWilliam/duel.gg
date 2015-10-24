package models.pinger

import javax.inject.{Inject, Singleton}

import akka.actor.ActorDSL._
import akka.actor.{Kill, PoisonPill, ActorSystem}
import akka.agent.Agent
import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.service.PingPongProcessor.{Ping, Ready, ReceivedBytes}
import gg.duel.pinger.service.{PingPongProcessorActor, PingPongProcessorState}
import models.games.GamesManager
import models.servers.ServerManager
import play.api.inject.ApplicationLifecycle
import play.api.libs.iteratee.Concurrent

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created on 13/07/2015.
 */
@Singleton
class PingerService @Inject()
(serverProvider: ServerManager,
  gamesManager: GamesManager)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem,
  applicationLifecycle: ApplicationLifecycle
  ) {
  
  val ourState = Agent(SIteratorState.empty)

  val (enumerator, channel) = Concurrent.broadcast[String]

  val conactor = actor(name = "wut")(new Act {

    val pingPong = context.actorOf(
      name = "ping-pong",
      props = PingPongProcessorActor.props(initialState = PingPongProcessorState.empty)
    )

    var currentState = SIteratorState.empty

    become {
      case Ready(_) =>
        import concurrent.duration._
        actorSystem.scheduler.schedule(0.seconds, 3.seconds, self, 'Ping)
        become {
          case 'Ping =>
            serverProvider.servers.servers.values.foreach(pingPong ! Ping(_))
          case r: ReceivedBytes =>
            currentState = currentState.next(r.toSauerBytes)
            ourState.send(currentState)
            currentState match {
              case SFoundGame(_, CompletedGame(Left(duel), _)) =>
                val updatedDuel = serverProvider.servers.servers.collectFirst {
                  case (serverName, server) if server.getAddress == duel.server =>
                    duel.copy(server = serverName)
                }.getOrElse(duel)
                gamesManager.addDuel(updatedDuel)
                channel.push(updatedDuel.toPrettyJson)
              case SFoundGame(_, CompletedGame(Right(ctf), _)) =>
                val updatedCtf = serverProvider.servers.servers.collectFirst {
                  case (serverName, server) if server.getAddress == ctf.server =>
                    ctf.copy(server = serverName)
                }.getOrElse(ctf)
                gamesManager.addCtf(updatedCtf)
                channel.push(updatedCtf.toPrettyJson)
              case _ =>
            }
        }
    }

  })
  applicationLifecycle.addStopHook(() => Future {
    conactor ! Kill
  })
}
