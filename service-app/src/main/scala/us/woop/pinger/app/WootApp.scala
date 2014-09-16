package us.woop.pinger.app

import akka.actor.ActorSystem
import com.hazelcast.core.Hazelcast
import us.woop.pinger.app.Woot.JournalGenerator
import us.woop.pinger.data.ServersListing
import us.woop.pinger.service.PingerController.Monitor
import us.{StandaloneWSAPI, WSAsyncDuelPersister}

object WootApp extends App {

  val persister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://127.0.0.1:8984", "db-stage", "ngnsads")
  implicit val as = ActorSystem("cooool")
  val tempHazelcastInstance = Hazelcast.newHazelcastInstance()
  val yay = Woot.props(tempHazelcastInstance, persister, JournalGenerator.standard, true)
  import akka.actor.ActorDSL._
  val container = actor(new Act{
    val main = as.actorOf(yay, "mainStuffs")
    for { server <- ServersListing.servers } {
      main ! Monitor(server)
    }
  })

}
