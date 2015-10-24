package models.pinger

import javax.inject.{Inject, Singleton}
import akka.actor.ActorDSL._
import akka.actor.{Kill, ActorSystem}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.service._
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

  val (enumerator, channel) = Concurrent.broadcast[String]

  val receiveActor = actor(name = "collect-games")(new Act {
    val pingerActor = actorSystem.actorOf(props = MultiplePinger.props, name = "multi-pinger")
    become {
      case scd: SimpleCompletedDuel =>
        channel.push(scd.toPrettyJson)
      case scc: SimpleCompletedCTF =>
        channel.push(scc.toPrettyJson)
    }

  })

  actorSystem

  applicationLifecycle.addStopHook(() => Future {
    receiveActor ! Kill
  })
}
