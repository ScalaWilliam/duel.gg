package us.woop.pinger.analytics.actor

import us.woop.pinger.PingPongProcessor
import PingPongProcessor.Server
import akka.actor._
import akka.actor.ActorDSL._
import us.woop.pinger.PingPongProcessor
import scalaz.concurrent.Task
import scala.xml.Elem
import scalaz.-\/
import scalaz.-\/
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import scalaz.\/-
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor
import IndividualGameCollectorActor.HaveGame
import us.woop.pinger.analytics.processing.Collector


class IndividualGameCollectorActor(server: Server) extends Act with Actor with ActorLogging {
  import scalaz.stream._
  val (queue, source) = scalaz.stream.async.queue[ParsedMessage]()
    val sink: Sink[Task, Elem] = Process.constant(a => Task.delay { self ! HaveGame(server, s"$a") })
//    val woot = source pipe Collector.getGame pipe Collector.processGame to sink
//    log.info("Initialising game processor for {}", server)
    case class FailDue(to: Throwable)
//    woot.run.runAsync{ r =>
//      log.info("Process for {} terminated because {}", server, r)
//      r match {
//        case -\/(a) => self ! FailDue(a)
//        case \/-(b) =>
//      }
//    }
    become {
      case m: ParsedMessage =>
        queue.enqueue(m)
      case g: HaveGame =>
        context.parent ! g
      case FailDue(to) =>
        throw to
    }
}
