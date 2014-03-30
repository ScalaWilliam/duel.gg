package us.woop.pinger

import akka.actor._

import scala.concurrent.duration._
import us.woop.pinger.PingerClient.{Ready, Ping}
import akka.event.LoggingReceive

class PingerService(injectPingerClient: Option[ActorRef] = None) extends Actor with ActorLogging with Stash with PingerServiceMixin {

  import PingerServiceData._

  lazy val settings = PingerServiceSettings(context.system)

  def this() = this(None)

  def this(injectedPingerClient: ActorRef) = this(Option(injectedPingerClient))

  val pingerClient = injectPingerClient.getOrElse {
    context.actorOf(Props(classOf[PingerClient], self), name = "pingerClient")
  }

  import context.dispatcher

  def schedule(a: FiniteDuration, b: FiniteDuration, d: Any) =
    context.system.scheduler.schedule(a, b, pingerClient, d)

  override def postStop() {
    for {(server, schedule) <- schedules} {
      log.debug("Canceling schedule {} for {}", schedule, server)
      schedule.cancel()
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

      pushSubscription(subscription, requiredInterval)

      context.watch(sender())

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
        subscription@(_, `subscriber`) <- subscriptions.keys.toList
      } yield subscription
      subscriptionsToRemove foreach removeSubscription
      if (firehose remove subscriber) {
        log.debug("{} requested firehose access")
      }

    case Firehose =>
      val client = sender()
      log.debug("{} requested firehose access")
      firehose += client
      context.watch(client)

    case Unfirehose =>
      log.debug("{} requested to revoke firehose access")
      firehose -= sender()

    case message@Tuple2((ip: String, port: Int), payload: Any) if sender() == pingerClient =>
      val server = Server(ip, port)
      val message = SauerbratenPong(
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
