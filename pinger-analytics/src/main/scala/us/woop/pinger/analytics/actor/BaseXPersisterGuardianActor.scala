package us.woop.pinger.analytics.actor

import akka.actor.ActorDSL._
import scala.Some
import scala.util.control.NonFatal
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor.HaveGame
import us.woop.pinger.data.actor.PingPongProcessor.Server
import akka.actor.{ActorIdentity, Identify, Terminated, ActorRef}
import com.xqj2.XQConnection2

class BaseXPersisterGuardianActor(connection: => XQConnection2) extends Act with ActWithStash {

  superviseWith(OneForOneStrategy() {
    case NonFatal(e) => Stop
  })

  def preparePersister() = {
    val persister = actor(context, name = "persister")(new BaseXPersisterActor(connection))
    context watch persister
    persister
  }
  var counter = 1
  whenStarting {
    val persister = preparePersister()
    become{normal(persister)}
    import concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
  }

  def schedulePersisterRestart() {
    import concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.scheduleOnce(5.seconds, self, Recover)
  }

  case object Recover

  def normal(persister: ActorRef): Receive = {
    case message: HaveGame =>
      persister ! message
    case Terminated(`persister`) =>
      context unwatch persister
      become(unalive)
      schedulePersisterRestart()
  }

  def unalive: Receive = {
    case Recover =>
      val newPersister = preparePersister()
      newPersister ! Identify(None)
    case Terminated(child) =>
      context unwatch child
      schedulePersisterRestart()
    case ActorIdentity(_, Some(persister)) =>
      become{normal(persister)}
      unstashAll()
    case _: HaveGame =>
      stash()
  }

}
