package us.woop.pinger

import us.woop.pinger.PingerServiceData.Server
import akka.actor.{ActorLogging, Cancellable, ActorRef}
import scala.concurrent.duration.FiniteDuration
import us.woop.pinger.PingerClient.Ping

trait PingerServiceMixin {
  this: ActorLogging =>

  def settings: PingerServiceSettingsImpl

  /** A view of subscribers per each server based on subscriptions above **/
  def serversSubscribers =
    subscriptions.groupBy {
      _._1._1
    }.mapValues {
      _.keys.map {
        _._2
      }
    }
  import concurrent.duration._
  lazy val initialDelay = 0.seconds
//  settings.subscribeToPingDelay

  type MinimumRate = Option[FiniteDuration]
  val subscriptions = scala.collection.mutable.Map[(Server, ActorRef), MinimumRate]()
  val schedules = scala.collection.mutable.Map[Server, Cancellable]()
  val firehose = scala.collection.mutable.Set[ActorRef]()

  /** Add a schedule if it's not already set **/
  def scheduleSafely(server: Server, initially: FiniteDuration = initialDelay) =
    for {
      interval <- pingInterval(server)
      ping = Ping((server.ip, server.port))
    } schedules.getOrElseUpdate(server, {
      log.debug("Starting schedule for {} at interval {}", server, interval)
      schedule(initially, interval, ping)
    })

  def schedule(a: FiniteDuration, b: FiniteDuration, d: Any): Cancellable

  val defaultPingInterval = 30.seconds

  /** *
    * Calculate a ping interval for a server. This is pretty much a map.
    * If there are no subscriptions it returns None,
    * If all the subscriptions lack a return rate we give it the default
    * Else we provide the minimum rate out of those that are set.
    * @param server The server to play with.
    * @return Optional finite duration
    */
  def pingInterval(server: Server): Option[FiniteDuration] = {
    val requiredRates = for {((`server`, _), requiredRate) <- subscriptions} yield requiredRate
    requiredRates.toList match {
      case Nil => None
      case rates => Option((for {
        requiredRate <- rates
        duration <- requiredRate
      } yield duration).reduceLeftOption((x, y) => if (x > y) y else x).getOrElse(defaultPingInterval))
    }
  }

  def removeSubscription(subscription: (Server, ActorRef)) {
    val server = subscription._1
    val oldInterval = pingInterval(server)

    /** Restart schedule if interval is higher **/
    /** Do nothing if interval does not change **/
    subscriptions remove subscription

    pingInterval(server) match {
      case None =>
        for {
          schedule <- schedules remove server
        } {
          log.debug("Canceling schedule for {}", schedule, server)
          schedule.cancel()
        }

      case Some(newIntervalDuration) =>
        for {
          oldIntervalDuration <- oldInterval
          if newIntervalDuration > oldIntervalDuration
          schedule <- schedules remove server
        } {
          log.debug("Increasing {} schedule interval from {} to {} ", server, oldIntervalDuration, newIntervalDuration)
          schedule.cancel()
          scheduleSafely(server)
        }
    }
  }

  def pushSubscription(subscription: (Server, ActorRef), requiredInterval: Option[FiniteDuration]) {
    val server = subscription._1
    /** Cancel current schedule if the demanded rate is higher **/
    val oldInterval = pingInterval(server)
    subscriptions += subscription -> requiredInterval
    val decreasedSchedule = for {
      intervalDuration <- requiredInterval
      oldIntervalDuration <- oldInterval
      if intervalDuration < oldIntervalDuration
      schedule <- schedules remove server
    } yield {
      log.debug("Decreasing {} schedule frequency from {} to {}", server, oldIntervalDuration, intervalDuration)
      schedule.cancel()
      scheduleSafely(server)
    }

    decreasedSchedule match {
      case None =>
        // not done anything to affect any schedules - so we're starting anew
        log.debug("No current schedules found - starting one for {} at rate {}", server, pingInterval(server))
        scheduleSafely(server)
      case _ =>
    }
  }
}
