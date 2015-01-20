package gg.duel.pinger.app

import akka.actor.ActorSystem
import com.hazelcast.core.Hazelcast
import gg.duel.pinger.app.Woot.JournalGenerator
import gg.duel.pinger.service.{StandaloneWSAPI, WSAsyncGamePersister}

object WootAppConfig {

}
object WootApp extends App {

  val persister = new WSAsyncGamePersister(
    client = new StandaloneWSAPI,
    basexContextPath = System.getProperty("pinger.basex.context", "http://127.0.0.1:8984"),
    dbName = System.getProperty("pinger.basex.name", "db-stage"),
    chars = System.getProperty("pinger.chars", "fntpykaq")
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
