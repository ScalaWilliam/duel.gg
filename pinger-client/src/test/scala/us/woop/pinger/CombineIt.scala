package us.woop.pinger

import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import us.woop.pinger.client.data.{PingPongProcessor, PersistRawData, MasterserverClientActor, GlobalPingerClient}
import us.woop.pinger.client.{MasterserverClientActor, GlobalPingerClient, PersistRawData}
import GlobalPingerClient.{Monitor, Listen}
import PingPongProcessor.Server
import java.io.File
import us.woop.pinger.client.data.MasterserverClientActor


object CombineIt extends App {

  val ha = ActorSystem("bang")

  val main = actor(ha, name = "main")(new GlobalPingerClient)

  val listener = actor(ha, name = "listener")(new Act {
    main ! Listen
    val ms = actor(context, name="masterserverClient")(new MasterserverClientActor)
    import MasterserverClientActor._

    {
      import scala.concurrent.ExecutionContext.Implicits.global
      import concurrent.duration._
      context.system.scheduler.schedule(1.second, 5.minutes, ms, RefreshServerList)
    }

    become {

      case MasterServers(servers) =>
        for { (ip, port) <- servers }
          main ! Monitor(Server(ip, port))

      case ServerAdded((ip, port)) =>
        main ! Monitor(Server(ip, port))

      case ServerGone(server) =>

//      case any => println(s"ah -> $any")
    }
  })


  val perst = actor(ha, name="persister")(new PersistRawData(new File("./sample-data")) {
    main ! Listen
  })

  main ! Monitor(Server("sauer.woop.us"))

}
