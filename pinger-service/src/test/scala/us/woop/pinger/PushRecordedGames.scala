package us.woop.pinger

import java.io.File

import akka.actor.ActorSystem
import com.amazonaws.regions.Regions
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.Options
import us.woop.pinger.analytics.applications.DatabaseReader
import us.woop.pinger.analytics.processing.StreamedDuelMaker.{ZRejectedDuelState, ZFoundGame, IteratorImplicit}
import us.woop.pinger.data.persistence.Format.Server
import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.DynamoDBAccess
import us.woop.pinger.service.publish.{PublishDuelsToDynamoDBActor, PublishRawMessagesToS3Actor}
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor.S3Access

object PushRecordedGames extends App with AmazonCredentials {
  val target = new File("***REMOVED***/Projects/14/ladder.sauer/19-21")
  val db = factory.open(target, new Options())
//  val servers = DatabaseReader.listServers(db)
//  servers foreach println
  val server = Server("188.226.169.46",28785)
  import IteratorImplicit._
  val serverDatazz = DatabaseReader.listServerData(db, server).asStateIterator

  val collectGames = serverDatazz collect {
    case a: ZFoundGame => a
  }

  implicit val as = ActorSystem("trol")

  val access = DynamoDBAccess(
    accessKeyId = accessKeyId,
    secretAccessKey = secretAccessKey,
    tableName = "DuelGGDuels",
    region = Regions.EU_WEST_1,
    myId = MyId.default
  )

  val act = as.actorOf(PublishDuelsToDynamoDBActor.props(access))

  var counter = 0
  collectGames foreach {
    m =>
      act ! m.completedDuel
      counter = counter + 1
  }
  println(s"Done! Added $counter items")
  as.shutdown()
}
