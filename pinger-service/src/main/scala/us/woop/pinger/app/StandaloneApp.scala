package us.woop.pinger.app

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import akka.actor._
import akka.routing.RoundRobinPool
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.Server
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController
import us.woop.pinger.service.PingerController.{Monitor, Unmonitor}
import us.woop.pinger.service.individual.ServerMonitor.ServerStateChanged

import scala.util.Try

object StandaloneApp extends App with StandaloneAppMBean {
  val sys = ActorSystem("YES")
  import akka.actor.ActorDSL._

  val main = actor(sys, "main")(new Act {

    val h2Publisher = actor(context, "h2Publisher")(new PublishToH2Actor)

    val pingerController =
      context.actorOf(RoundRobinPool(1, supervisorStrategy =  OneForOneStrategy(){
        case _: ActorInitializationException => SupervisorStrategy.Restart
        case _: DeathPactException  => Stop
        case _: ActorKilledException => SupervisorStrategy.Restart
        case _: Exception => SupervisorStrategy.Resume
      }).props(PingerController.props(context.self)),
        "pingerController")

    become {
      case r: ReceivedBytes =>
        h2Publisher ! r
      case m: Monitor =>
        pingerController ! m
      case u: Unmonitor =>
        pingerController ! u
    }
  })

  val servers = """
    |rb0.butchers.su
    |rb1.butchers.su
    |rb1.butchers.su 20000
    |rb2.butchers.su
    |rb3.butchers.su
    |vaq-clan.de
    |effic.me 10000
    |effic.me 20000
    |effic.me 30000
    |effic.me 40000
    |effic.me 50000
    |effic.me 60000
    |psl.sauerleague.org 20000
    |psl.sauerleague.org 30000
    |psl.sauerleague.org 40000
    |psl.sauerleague.org 50000
    |psl.sauerleague.org 60000
    |sauer.woop.us
    |vaq-clan.de
    |butchers.su
    |noviteam.de
    |darkkeepers.dk:28786
    |""".stripMargin.split("\r?\n").filterNot(_.isEmpty).toVector.flatMap(h => Try(Server.fromAddress(h)).toOption.toVector)


  for { server <- servers } {
    main ! Monitor(server)
  }

//  val mbeanServer = ManagementFactory.getPlatformMBeanServer
//
//  val mbeanName = new ObjectName("us.woop.pinger.app:type=Main")
//
//  mbeanServer.registerMBean(this, mbeanName)

}
