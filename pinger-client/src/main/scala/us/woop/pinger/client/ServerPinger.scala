package us.woop.pinger.client

import akka.actor.ActorDSL._
import akka.actor._
import us.woop.pinger.client.ServerPinger._
import us.woop.pinger.client.PingPongProcessor._
import scala.concurrent.duration._
import us.woop.pinger.client.ServerPinger.ServerPingerParams
import us.woop.pinger.client.PingPongProcessor.Ping
import us.woop.pinger.client.ServerPinger.PingRate
import us.woop.pinger.client.PingPongProcessor.ParsedMessage
import us.woop.pinger.SauerbratenServerData.ServerInfoReply
import akka.actor.Terminated
import us.woop.pinger.client.PingPongProcessor.Ready

object ServerPinger {

  case object Refresh

  sealed abstract class PingRate(val at: FiniteDuration)

  case object Fast extends PingRate(3 seconds)

  case object Medium extends PingRate(5 seconds)

  case object Slow extends PingRate(30 seconds)

  case class Server(host: String, port: Int)

  class TooManyMessagesException(rate: Int, limit: Int) extends RuntimeException(s"Received $rate mps, limit $limit")

  class PingerDiedException(actor: ActorRef) extends RuntimeException("Pinger has died")

  case class ServerPingerParams(server: Server, maxMessagesPerSecond: Int, requiredRate: Option[PingRate])

  def buildStandard(hostPair: (String, Int)) = Props(new ServerPinger(ServerPingerParams(Server(hostPair._1, hostPair._2), 100, None)))
  def buildStandardWithRate(hostPair: (String, Int), requireRate: PingRate) = Props(new ServerPinger(ServerPingerParams(Server(hostPair._1, hostPair._2), 100, Option(requireRate))))

  case class RequireRate(rate: Option[PingRate])

}

class ServerPinger(val params: ServerPingerParams) extends Act with ActorLogging {

  val hostPair = (params.server.host, params.server.port)
  var players: Int = _
  var pinger: ActorRef = _
  var rate: PingRate = _
  var lastUpdated: Long = _
  var totalMessages: Int = _
  var badHashes: Int = _
  var refreshSchedule: Cancellable = _
  val meter = context.actorSelection("/user/metrics")
  var requiredRate = params.requiredRate

  superviseWith {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case _: CannotParse => Escalate
      case _: ActorKilledException => Stop
      case _: Exception => Restart
    }
  }

  whenStopping {
    context.stop(pinger)
    context.unwatch(pinger)
  }

  whenStarting {
    pinger = actor(factory = context, name = "pinger")(new FullPingPongProcessor())
    context watch pinger
    pinger ! AreYouReady
  }

  whenRestarted {
    case _: PingerDiedException =>
      pinger = actor(factory = context, name = "pinger")(new FullPingPongProcessor())
      context watch pinger
      pinger ! AreYouReady
    case _ =>
  }

  become {
    case RequireRate(value) =>
      requiredRate = value
    case Ready(_) =>

      badHashes = 0
      totalMessages = 0
      lastUpdated = System.currentTimeMillis() / 1000
      pinger ! Ping(hostPair)
      become {

        case RequireRate(value) =>
          requiredRate = value
        case b: BadHash =>
          badHashes = badHashes + 1
          totalMessages = totalMessages + 1

        case m @ ParsedMessage(`hostPair`, _, decoded: ServerInfoReply) =>
          totalMessages = totalMessages + 1
          players = decoded.clients
          context.parent forward m

          refreshSchedule = {
            import scala.concurrent.ExecutionContext.Implicits.global
            context.system.scheduler.scheduleOnce(3.seconds, self, Refresh)
          }

          become {

            case RequireRate(value) =>
              requiredRate = value
            case m @ ParsedMessage(`hostPair`, _, decoded: ServerInfoReply) =>
              players = decoded.clients
              context.parent forward m
              totalMessages = totalMessages + 1

            case m : ParsedMessage =>
              context.parent forward m
              totalMessages = totalMessages + 1

            case b: BadHash =>
              badHashes = badHashes + 1
              totalMessages = totalMessages + 1

            case Terminated(actor) if actor == pinger =>
              throw new PingerDiedException(pinger)

            case Refresh =>
              pinger ! Ping(hostPair)

              {
                val previousRate = rate
                val possibleRate = if (players > 6) Fast else if (players > 2) Medium else Slow
                rate = requiredRate match {
                  case Some(standardRate) if standardRate.at < possibleRate.at => standardRate
                  case None => possibleRate
                }
                import scala.concurrent.ExecutionContext.Implicits.global
                (rate, Option(previousRate)) match {
                  case (`previousRate`, _) =>
                  case (newRate, None) => log.info("Initialising {} with rate {}", hostPair, newRate)
                  case (newRate, _) => log.info("Changing {} rate from {} to {}", hostPair, newRate, previousRate)
                }
                refreshSchedule = context.system.scheduler.scheduleOnce(rate.at, self, Refresh)
              }

              val now = System.currentTimeMillis() / 1000
              val dt = (now - lastUpdated).toInt
              if ( dt > 0 ) {
                val currentRate = totalMessages / dt
                if (currentRate > params.maxMessagesPerSecond) {
                  throw new TooManyMessagesException(rate = currentRate, params.maxMessagesPerSecond)
                }
              }

              val metrics = 'Metered -> List(
                'hostPair -> hostPair,
                'players -> players,
                'period -> dt,
                'badHashes -> badHashes,
                'totalMessages -> totalMessages,
                'rate -> rate
              )

              meter ! metrics

              badHashes = 0
              totalMessages = 0
              lastUpdated = now

          }

        case m : ParsedMessage =>
          totalMessages = totalMessages + 1
          context.parent forward m

        case Terminated(actor) if actor == pinger =>
          throw new PingerDiedException(pinger)

      }
  }

}
