package us.woop.pinger.app

import akka.actor._
import akka.routing.RoundRobinPool
import us.woop.pinger.data.{ServersListing, Server}
import us.woop.pinger.data.journal.IterationMetaData
import us.woop.pinger.service.analytics.ProcessGames
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController
import us.woop.pinger.service.PingerController.{Monitor, Unmonitor}
import us.woop.pinger.service.delivery.JournalSauerBytes
import JournalSauerBytes.WritingStopped

object StandaloneApp extends App {

  val sys = ActorSystem("YES")
  import akka.actor.ActorDSL._

  val main = actor(sys, "main")(new Act {

    val filePublisher = actor(context, "filePublisher")(new JournalSauerBytes(IterationMetaData.build))

    val duelProcessor = actor(context, "duelProcessor")(new ProcessGames)

    val pingerController =
      context.actorOf(RoundRobinPool(1, supervisorStrategy = OneForOneStrategy(){
        case _: ActorInitializationException => SupervisorStrategy.Restart
        case _: DeathPactException  => Stop
        case _: ActorKilledException => SupervisorStrategy.Restart
        case _: Exception => SupervisorStrategy.Resume
      }).props(PingerController.props(disableHashing = true, context.self)),
        "pingerController")

    var currentMetaData: IterationMetaData = _

    become {
      case r: ReceivedBytes =>
        filePublisher ! r
        duelProcessor ! r
      case WritingStopped(metaData)  =>
//        metaData
      case m: IterationMetaData  =>
        duelProcessor ! m
//        couchPublisher ! m
//      case a: Completed Duel =>
//        couchPublisher ! a
      case m: Monitor =>
        pingerController ! m
      case u: Unmonitor =>
        pingerController ! u
    }
  })



  for { server <- ServersListing.servers } {
    main ! Monitor(server)
  }

//  val mbeanServer = ManagementFactory.getPlatformMBeanServer
//
//  val mbeanName = new ObjectName("us.woop.pinger.app:type=Main")
//
//  mbeanServer.registerMBean(this, mbeanName)

}
