package gg.duel.pinger.app

import akka.actor.ActorSystem
import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.{StrictLogging, LazyLogging}
import gg.duel.pinger.app.Woot.JournalGenerator
import gg.duel.pinger.service.WSAsyncGamePersister

import scala.util.control.NonFatal

object WootAppConfig {
  val config = ConfigFactory.load()
  val basexContext = config.getString("pinger.basex.context")
  val dbName = config.getString("pinger.basex.name")
  val chars = config.getString("pinger.chars")
}

object WootApp extends App with StrictLogging {
  System.setProperty("hazelcast.logging.type", System.getProperty("hazelcast.logging.type", "slf4j"))
  logger.info(s"Configuration: ${WootAppConfig.config}")

  var tempHazelcastInstance: HazelcastInstance = _

  implicit val as = ActorSystem("cooool")
  val pipeline = {
    import spray.http._
    import spray.httpx.encoding.{Gzip, Deflate}
    import spray.httpx.SprayJsonSupport._
    import spray.client.pipelining._
    import as.dispatcher

    sendReceive
  }
  val persister = new WSAsyncGamePersister(
    client = pipeline,
    basexContextPath = WootAppConfig.basexContext,
    dbName = WootAppConfig.dbName,
    chars = WootAppConfig.chars
  )

  try {
    import scala.concurrent.ExecutionContext.Implicits.global
    import concurrent.duration._
    scala.concurrent.Await.result(persister.connects, 5.seconds)
    scala.concurrent.Await.result(persister.retrieveServers, 5.seconds)
    tempHazelcastInstance = Hazelcast.newHazelcastInstance()
  } catch {
    case NonFatal(e) =>
      as.shutdown()
      as.awaitTermination()
  }

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

  as.awaitTermination()
  as.shutdown()
  Option(tempHazelcastInstance).foreach(_.shutdown())

}
