package us.woop.pinger

import akka.actor._

import scala.concurrent.duration._
import us.woop.pinger.PingerClient.{Ready, Ping}
import akka.event.LoggingReceive

class PingerService(injectPingerClient: Option[ActorRef] = None) extends Actor with ActorLogging with Stash {

  import PingerServiceData._

  val settings = PingerServiceSettings(context.system)
  def this() = this(None)
  def this(injectedPingerClient: ActorRef) = this(Option(injectedPingerClient))
  val pingerClient = injectPingerClient.getOrElse{context.actorOf(Props(classOf[PingerClient], self), name = "pingerClient")}

  type MinimumRate = Option[FiniteDuration]

  val subscriptions = scala.collection.mutable.Map[(Server, ActorRef), MinimumRate]()
  val schedules = scala.collection.mutable.Map[Server, Cancellable]()
  val firehose = scala.collection.mutable.Set[ActorRef]()

  /** A view of subscribers per each server based on subscriptions above **/
  def serversSubscribers =
    subscriptions.groupBy{_._1._1}.mapValues{_.keys.map{_._2}}

  /** *
    * Calculate a ping interval for a server. This is pretty much a map.
    * If there are no subscriptions it returns None,
    * If all the subscriptions lack a return rate we give it the default
    * Else we provide the minimum rate out of those that are set.
    * @param server The server to play with.
    * @return Optional finite duration
    */
  def pingInterval(server: Server): Option[FiniteDuration]= {
    val requiredRates = for { ((`server`, _), requiredRate) <- subscriptions } yield requiredRate
    requiredRates.toList match {
      case Nil => None
      case rates => Option((for {
        requiredRate <- rates
        duration <- requiredRate
      } yield duration).reduceLeftOption((x, y) => if (x > y) y else x).getOrElse(settings.defaultPingInterval))
    }
  }

  import context.dispatcher
  /** Add a schedule if it's not already set **/
  def scheduleSafely(server: Server, initially: FiniteDuration = initialDelay) =
    for {
      interval <- pingInterval(server)
      ping = Ping((server.ip, server.port))
    } schedules.getOrElseUpdate(server, {
      log.debug("Starting schedule for {} at interval {}", server, interval)
      context.system.scheduler.schedule(initially, interval, pingerClient, ping)
    })

  val initialDelay = settings.subscribeToPingDelay

  override def postStop() {
    for { (server, schedule) <- schedules } {
      log.debug("Canceling schedule {} for {}", schedule, server)
      schedule.cancel()
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

  def ready = LoggingReceive {
    case Subscribe(server, requiredInterval) =>

      val subscription = server -> sender()

      subscriptions get subscription match {
        case Some(previousInterval) =>
          log.debug("Subscription {} requests to change its rate from {} to {}", subscription, previousInterval, requiredInterval)
        case None =>
          log.debug("Requesting subscription {} at rate {}", subscription, requiredInterval)
      }

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
          context.watch(sender())
        case _ =>
      }

    /**
      * Unsubscribe the subscriber from the server.
      * If the server has no more subscribers, it gets removed.
      */
    case Unsubscribe(server) =>
      val subscription = server -> sender()
      log.debug("Unsubscribe request for {}", subscription)
      removeSubscription(subscription)

    case Terminated(subscriber) =>
      log.debug("Watched subscriber {} has terminated", subscriber)
      val subscriptionsToRemove = for {
        subscription @ (_, `subscriber`) <- subscriptions.keys.toList
      } yield subscription
      subscriptionsToRemove foreach removeSubscription
      if ( firehose remove subscriber ) {
        log.debug("{} requested firehose access")
      }

    case Firehose =>
      val client = sender()
      log.debug("{} requested firehose access")
      firehose += client
      context.watch(client)

    case Unfirehose  =>
      log.debug("{} requested to revoke firehose access")
      firehose -= sender()

    case message @ Tuple2((ip: String, port: Int), payload: Any) if sender() == pingerClient =>
      val server = Server(ip, port)
      val message = SauerbratenPong (
        unixTime = System.currentTimeMillis() / 1000L,
        host = (ip, port),
        payload = payload
      )
      for {
        subscribers <- serversSubscribers get server
        subscriber <- subscribers
      } subscriber ! message
      for {
        hose <- firehose
      } hose ! message
  }
  def receive = {
    case Ready(_) =>
      log.debug("{} is now ready", sender())
      context.become(ready)
      unstashAll()
    case other =>
      stash()
  }
}
