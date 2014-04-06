package us.woop.pinger

import akka.actor.{ActorLogging, Actor}
import us.woop.pinger.WoopMonitoring.MonitorMessage

trait WoopMonitoring extends Actor with ActorLogging {
  private val monitorActor = context.actorSelection("/user/monitor")
  def notify(withValue: MonitorMessage) =
    monitorActor ! withValue
  def sendMetrics(key: Symbol, value: Any*) =
    notify(MonitorMessage(key, value:_*))
}
object WoopMonitoring {
  case class MonitorMessage(key: Symbol, value: Any*)
}
