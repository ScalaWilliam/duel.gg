package services

import java.io.File
import java.time.LocalDateTime
import javax.inject._

import akka.actor.{Kill, ActorSystem}
import gg.duel.pinger.data.journal.{SauerBytes, JournalWriter}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.iteratee.Concurrent

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by William on 27/10/2015.
 */

@Singleton
class ServeLiveSauerBytesService @Inject()(applicationLifecycle: ApplicationLifecycle)(implicit actorSystem: ActorSystem,
                                                                                       executionContext: ExecutionContext) {

  import akka.actor.ActorDSL._

  val (enumerator, channel) = Concurrent.broadcast[SauerBytes]

  val myActor = actor(name = "broadcaster")(new Act {
    become {
      case sauerBytes: SauerBytes =>
        channel.push(sauerBytes)
    }
  })

  actorSystem.eventStream.subscribe(myActor, classOf[SauerBytes])

  applicationLifecycle.addStopHook(() => Future {
    myActor ! Kill
  })

}