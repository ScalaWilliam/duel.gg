package us.woop.pinger.analytics.applications

import java.io.{PrintWriter, File}
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.Options
import us.woop.pinger.analytics.data.GameData
import us.woop.pinger.data.ParsedPongs.{PlayerExtInfo, ParsedMessage}
import scala.annotation.tailrec
import us.woop.pinger.analytics.processing.Collector._
import us.woop.pinger.analytics.processing.Collector
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.ParsedTypedMessageConvertedServerInfoReply
import org.joda.time.DateTime
import us.woop.pinger.data.persistence.Format


object DatabaseReaderApp extends App {

  val targets = for {
//    id <- List("13-16", "12-11", "14-06", "15-06", "13-15")
    id <- List("15-06")
//    filename = s"/home/william/Projects/14/ladder.sauer/indexed-data/collected/$id"
    filename = "/home/william/Projects/14/ladder.sauer/indexed-data/collected/2/15-06"
  } yield new File(filename)

  println(s"Not found ==> ${targets.filterNot{_.exists}}")

  val target = new File("/home/william/Projects/14/ladder.sauer/indexed-data/collected/2/15-06")
  val db = factory.open(target, new Options())
  val messages = DatabaseReader.listServerData(db,Format.Server("95.85.28.218", 20000) )
  val yes = messages filter { _.time > 1397913491763L }
//  val ouch = for { ParsedMessage(_, t, m: PlayerExtInfo) <- yes } yield t -> m
  val ouch = yes
  val pw = new PrintWriter(new File("/home/william/fuck.txt"))
  ouch foreach pw.println
  pw.close()
  //    game <- Collector.extractData(messages)
  //    xml <- Collector.processGameData(game).right.toSeq
  //  } yield xml

//  } yield t -> m
//  val output = for {
//    target <- targets
//    db = factory.open(target, new Options())
//    servers = DatabaseReader.listServers(db).toList
//
//    server <- servers.toList
//    if server.ip == "95.85.28.218" && server.port == 20000
//    messages = DatabaseReader.listServerData(db, server).filter {
//      _.time > 1397907860111L
//    }
//    ParsedMessage(_, t, m: PlayerExtInfo) <- messages
//  //    game <- Collector.extractData(messages)
//  //    xml <- Collector.processGameData(game).right.toSeq
//  //  } yield xml
//
//  } yield t -> m

//  println(output.toList mkString "\n")
//  val exy = <games>{output.toStream.toSeq}</games>
//  val pw = new PrintWriter(new File("/home/william/woot.xml"))
//  pw.println("<games>")
//  output foreach pw.println
//  pw.println("</games>")
//  pw.close()
//  Thread.sleep(50)
}
