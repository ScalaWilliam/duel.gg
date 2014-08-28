package us.woop.pinger.app

import akka.actor.ActorSystem
import com.hazelcast.config.{ManagementCenterConfig, Config}
import com.hazelcast.core.Hazelcast
import us.woop.pinger.app.Woot.JournalGenerator
import us.woop.pinger.data.ServersListing
import us.woop.pinger.service.PingerController.Monitor
import us.{WSAsyncDuelPersister, StandaloneWSAPI}

object WootApp extends App {

  val persister = new WSAsyncDuelPersister(new StandaloneWSAPI, "http://127.0.0.1:8984", "db-stage", "ngnsads")
  implicit val as = ActorSystem("cooool")
  val config = new Config
  config.setManagementCenterConfig(new ManagementCenterConfig("http://localhost:8091/mancenter", 5))
  config.getManagementCenterConfig.setEnabled(true)
  config.setLicenseKey("CBGAEHONFMI12W111700370Q67009Z")
  config.getGroupConfig.setName("db-stage")
  val tempHazelcastInstance = Hazelcast.newHazelcastInstance(config)
  val yay = Woot.props(tempHazelcastInstance, persister, JournalGenerator.standard)
  val main = as.actorOf(yay, "mainStuffs")
  for { server <- ServersListing.servers } {
    main ! Monitor(server)
  }

}
