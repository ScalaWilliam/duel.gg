package us.woop.pinger

import akka.actor._

import scala.concurrent.duration._
import us.woop.pinger.PingerClient.{Ready, Ping}
import akka.event.LoggingReceive

class PingerService(injectPingerClient: Option[ActorRef] = None) extends Actor with ActorLogging with Stash {

  import PingerServiceData._

  def this() = this(None)
  def this(injectedPingerClient: ActorRef) = this(Option(injectedPingerClient))
  val pingerClient = injectPingerClient.getOrElse{context.actorOf(Props(classOf[PingerClient], self), name = "pingerClient")}

  val subscriptions = scala.collection.mutable.Set[(Server, ActorRef)]()
  val schedules = scala.collection.mutable.Map[Server, Cancellable]()

  /** A view of subscribers per each server based on subscriptions above **/
  def serversSubscribers = subscriptions.groupBy{_._1}.mapValues{_.map{_._2}}
  val rates = scala.collection.mutable.Map[Server, FiniteDuration]().withDefault(serverName => 30.seconds)

  import context.dispatcher
  def receive = {
    case Ready(_) =>
      log.debug("{} is now ready", sender())
      context.become(ready)
      unstashAll()
    case other =>
      stash()
  }

  val initialDelay = 1.second

  override def postStop() {
    for { (server, schedule) <- schedules } {
      log.debug("Canceling schedule {} for {}", schedule, server)
      schedule.cancel()
    }
  }

  def ready = LoggingReceive {
    case Subscribe(server) =>
      log.debug("Client {} wants to subscribe to {}", sender(), server)
      log.debug("Updating schedule for {} at rate {}, initially in {}", server, rates(server), initialDelay)
      schedules.getOrElseUpdate(server, context.system.scheduler.schedule(initialDelay, rates(server), pingerClient, Ping((server.ip, server.port))))
      subscriptions += server -> sender()
      context.watch(sender())
    /**
      * Unsubscribe the subscriber from the server.
      * If the server has no more subscribers, it gets removed.
      */
    case Unsubscribe(server) =>
      val subscription = server -> sender()
      log.debug("Client {} wants to unsubscribe from {}", sender(), server)
      subscriptions.remove(subscription)
      for {
        (server, schedule) <- schedules
        if serversSubscribers.get(server) == None
        schedule <- schedules remove server
      } {
        log.debug("Canceling schedule {} for {}", schedule, server)
        schedule.cancel()
      }
    /**
     * Change the polling rate of each server.
     */
    case ChangeRate(server, rate) =>
      log.debug("Client {} wants to change {} rate to {}", sender(), server, rate)
      rates += server -> rate
      schedules get server match {
        case Some(schedule) =>
          log.debug("Canceling schedule {} for {}", schedule, server)
          schedule.cancel()
          log.debug("Adding schedule for {} at rate {}, initially in {}", server, rates(server), initialDelay)
          schedules += server -> context.system.scheduler.schedule(initialDelay, rates(server), pingerClient, Ping((server.ip, server.port)))
        case None =>
      }

    case Terminated(subscriber) =>
      log.debug("Watched subscriber {} has terminated", subscriber)
      context.unwatch(subscriber)
      val removeSubscriptions =
        subscriptions.filter{_._2 == subscriber}
      for { subscription <- removeSubscriptions }
        subscriptions.remove(subscription)
      for {
        (server, schedule) <- schedules
        if serversSubscribers.get(server) == None
        schedule <- schedules remove server
      } {
        log.debug("Canceling schedule {} for {}", schedule, server)
        schedule.cancel()
      }

    case message @ Tuple2((ip: String, port: Int), payload: Any) if sender() == pingerClient =>
      val server = Server(ip, port)
      for {
        subscribers <- serversSubscribers get server
        subscriber <- subscribers
      } subscriber ! SauerbratenPong (
        unixTime = System.currentTimeMillis() / 1000L,
        host = (ip, port),
        payload = payload
      )
  }
}
