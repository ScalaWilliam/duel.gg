package us.woop.pinger.app

import akka.actor.ActorSystem
import com.hazelcast.core.Hazelcast
import us.woop.pinger.app.Woot.JournalGenerator
import us.{StandaloneWSAPI, WSAsyncGamePersister}

object WootApp extends App {

  val persister = new WSAsyncGamePersister(
    client = new StandaloneWSAPI,
    basexContextPath = System.getProperty("pinger.basex.context", "http://127.0.0.1:8984"),
    dbName = System.getProperty("pinger.basex.name", "db-stage"),
    chars = System.getProperty("pinger.chars", "bungabunga")
  )
  implicit val as = ActorSystem("cooool")
  val tempHazelcastInstance = Hazelcast.newHazelcastInstance()
  val disableHashing = System.getProperty("pinger.disable-hashing", "true") == "true"
  val yay = Woot.props(
    hazelcast = tempHazelcastInstance,
    persister = persister,
    journalGenerator = JournalGenerator.standard,
    disableHashing = disableHashing
  )
  import akka.actor.ActorDSL._
  val container = actor(new Act {

    val main = as.actorOf(yay, "mainStuffs")


  })

}
