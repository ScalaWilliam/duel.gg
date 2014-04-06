package us.woop.pinger
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import us.woop.pinger.WoopMonitoring.MonitorMessage

class WoopMonitor extends Act with ActorLogging {
  become {
    case message @ MonitorMessage(key, data @ _*) =>
      log.info("Received monitoring message {}", message)
    case other =>
      log.info(s"wututu $other")
  }
}
