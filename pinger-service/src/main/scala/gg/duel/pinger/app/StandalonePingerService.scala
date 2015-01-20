package gg.duel.pinger.app

import java.lang.management.ManagementFactory
import javax.management.ObjectName
import akka.actor._
import akka.kernel.Bootable
import akka.routing.RoundRobinPool
import gg.duel.pinger.data.Server
import gg.duel.pinger.service.PingerController.{Monitor, Unmonitor}
import gg.duel.pinger.service.PingerController

class HelloKernel extends Bootable {

  val system = ActorSystem("hellokernel")

  val controller = new StandalonePingerService(system)

  override def startup(): Unit = {
    controller.initialise()
  }

  override def shutdown(): Unit = {
    controller.shutdown()
  }

}

class StandalonePingerService(val system: ActorSystem) {

  def mbeanServer = ManagementFactory.getPlatformMBeanServer

  def mbeanName = new ObjectName("gg.duel.pinger.app:type=Main")

  def initialise(): Unit = {
    System.setProperty("hazelcast.logging.type", "slf4j")
    System.setProperty("hazelcast.jmx", "true")
    mbeanServer.registerMBean(this, mbeanName)
    system.actorOf(AppActor.props, name = "appActor")
  }

  def shutdown(): Unit = {
    mbeanServer.unregisterMBean(mbeanName)
    system.shutdown()
  }

  def restartHazelcast(): Unit = {
    system.actorSelection("/user/appActor/hazelcast/*") ! Kill
  }

}

import akka.actor.ActorDSL._
class AppActor extends Act {

  self ! Monitor(Server("85.214.66.181", 2000))
  // Restart Hazelcast when it's stopped
//  val hazelcast =
//    context.actorOf(RoundRobinPool(1, supervisorStrategy =  OneForOneStrategy(){
//      case _: ActorInitializationException => SupervisorStrategy.Restart
//      case _: ActorKilledException => SupervisorStrategy.Restart
//      case _: HazelcastConnectionLost => SupervisorStrategy.Restart
//      case _: DeathPactException => SupervisorStrategy.Stop
//      case _: Exception => SupervisorStrategy.Restart
//    }).props(HazelcastBridgeActor.props(Hazelcast.newHazelcastInstance,Option(context.self))),
//      "hazelcast")

  val pingerController =
    context.actorOf(RoundRobinPool(1, supervisorStrategy =  OneForOneStrategy(){
      case _: ActorInitializationException => SupervisorStrategy.Restart
      case _: DeathPactException  => Stop
      case _: ActorKilledException => SupervisorStrategy.Restart
      case _: Exception => SupervisorStrategy.Resume
    }).props(PingerController.props(disableHashing = true, context.self)),
      "pingerController")

  become {
//    case p: ParsedMessage =>
//      hazelcast ! p
//    case s: ServerStateChanged =>
//      hazelcast ! s
//    case r: ReceivedBytes =>
//      hazelcast ! r
    case m: Monitor =>
      pingerController ! m
    case u: Unmonitor =>
      pingerController ! u
  }
}

object AppActor {
  def props = Props(classOf[AppActor])
}

object StandalonePingerService extends App {
  val control = new StandalonePingerService(ActorSystem("YAY"))
  control.initialise()
}
