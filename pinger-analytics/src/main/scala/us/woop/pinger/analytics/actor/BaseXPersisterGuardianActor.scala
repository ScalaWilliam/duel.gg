package us.woop.pinger.analytics.actor

import akka.actor.ActorDSL._
import scala.Some
import scala.util.control.NonFatal
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor.HaveGame
import us.woop.pinger.data.actor.PingPongProcessor.Server
import akka.actor._
import com.xqj2.XQConnection2
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor.HaveGame
import akka.actor.Terminated
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify

class BaseXPersisterGuardianActor(connection: => XQConnection2) extends Act with ActWithStash with ActorLogging {

  superviseWith(OneForOneStrategy() {
    case NonFatal(e) => Stop
  })

  def preparePersister() = {
    log.info("Launching BaseX persister")
    val persister = actor(context, name = "persister")(new BaseXPersisterActor(connection))
    context watch persister
    persister
  }

  var counter = 1
  whenStarting {
    log.info("Starting BaseX persister guardian...")
    val persister = preparePersister()
    become{normal(persister)}
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
      log.info("BaseX persister guardian received message for {}", message.server)
    case Terminated(`persister`) =>
      log.info("BaseX persister has gone offline - {}", persister)
      context unwatch persister
      become(unalive)
      schedulePersisterRestart()
  }

  def unalive: Receive = {
    case Recover =>
      val newPersister = preparePersister()
      newPersister ! Identify(None)
    case Terminated(child) =>
      log.debug("Persister is still offline...")
      context unwatch child
      schedulePersisterRestart()
    case ActorIdentity(_, Some(persister)) =>
      log.info("Persister is back online - {}", persister)
      become{normal(persister)}
      unstashAll()
    case _: HaveGame =>
      log.info("Received a game message, stashing it")
      stash()
  }

}
