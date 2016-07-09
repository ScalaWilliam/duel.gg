package gg.duel.pingerservice

import akka.actor.ActorDSL._
import akka.actor.{ActorSystem, Kill}
import akka.agent.Agent
import gg.duel.pinger.mulprocess.{LiveProcessor, NewProcessor}
import gg.duel.pinger.service.PingPongProcessor.{Ping, Ready, ReceivedBytes}
import gg.duel.pinger.service.{PingPongProcessorActor, PingPongProcessorState}
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent

import scala.concurrent.ExecutionContext

/**
 * Created on 13/07/2015.
 */
class PingerService
(serverProvider: ServerManager,
  gamesManager: GamesManagerService)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem
  ) {

  val (enumerator, channel) = Concurrent.broadcast[Event]

  val (liveGameEnumerator, liveGameChannel) = Concurrent.broadcast[Event]

  val ourState = Agent(NewProcessor.empty)

  val conactor = actor(name = "wut")(new Act {

    val pingPong = context.actorOf(
      name = "ping-pong",
      props = PingPongProcessorActor.props(initialState = PingPongProcessorState.empty)
    )

    var currentState = NewProcessor.empty

    var liveDuelsState = LiveProcessor.empty


    case object CleanUpLiveDuels

    import concurrent.duration._
    context.system.scheduler.schedule(2.minutes, 1.minute, self, CleanUpLiveDuels)

    implicit def liveEventToEsEvent(liveEvent: gg.duel.pinger.mulprocess.Event): Event = Event(
      id = liveEvent.id,
      data = liveEvent.data,
      name = liveEvent.name
    )

    become {
      case Ready(_) =>
        import concurrent.duration._
        actorSystem.scheduler.schedule(0.seconds, 3.seconds, self, 'Ping)
//        actorSystem.scheduler.schedule(0.seconds, 3.seconds)(context.system.event, 'Ping)
        become {
          case 'Ping =>
            serverProvider.servers.servers.values.foreach(pingPong ! Ping(_))
          case CleanUpLiveDuels =>
            liveDuelsState.cleanUp.foreach { case (events, nextState) =>
              events.foreach { event =>
                liveGameChannel.push(event)
                push(event)
              }
              liveDuelsState = nextState
            }
          case r: ReceivedBytes =>
            context.system.eventStream.publish(r.toSauerBytes)
            currentState.receiveBytes(r) match {
              case (events, duelO, nextNewProcessor) =>

                for {
                  previousServerState <- currentState.sIteratorState.mIteratorState.serverStates.get(r.server)
                  currentServerState <- nextNewProcessor.sIteratorState.mIteratorState.serverStates.get(r.server)
                } {
                  liveDuelsState.stateChange(
                    server = r.server,
                    previousState = previousServerState,
                    currentState = currentServerState
                  ).foreach{ case (eventO, liveProcessor) =>
                    liveDuelsState = liveProcessor
                      eventO.foreach{ event =>
                        liveGameChannel.push(event)
                        push(event)
                      }
                  }
                }

                events.foreach { e => channel.push(e)
                  push(e)
                }
                duelO.foreach(duel => gamesManager.addDuel(duel))
                currentState = nextNewProcessor
                ourState.send(nextNewProcessor)

            }
        }
    }

  })
  def stop(): Unit = {
    conactor ! Kill
  }
  def push(e: gg.duel.pinger.mulprocess.Event): Unit = {
    actorSystem.eventStream.publish(e)
  }
}
