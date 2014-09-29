package us.woop.pinger.analytics.better
import java.io.{File, FileInputStream}
import us.woop.pinger.analytics.better.BetterMultiplexedReader.{SIteratorState, SInitial}
import us.woop.pinger.analytics.worse.MultiplexedDuelReader
import us.woop.pinger.data.journal.SauerBytesWriter
object CompareOldReaderApp extends App {

  val fah = new File("/home/william/Projects/37/ladder.sauer/data/sb-sb-20140901-1121-87b1cd20.log")

  def getSauerBytes = {
    val get = SauerBytesWriter.inputStreamNumBytes(new FileInputStream(fah))
    Iterator.continually(SauerBytesWriter.readSauerBytes(get)).takeWhile(_.isDefined).map(_.get)
  }

//  val gs = getSauerBytes.filter(_.server.ip.ip == "85.214.66.181").filter(_.server.port==20000)

//  gs.scanLeft(SInitial: SIteratorState)(_.next(_)) drop 1080 take 1 foreach println
//
//  gs take 50 foreach println
//
//  val kiki = for {
//  a <- gs
//  ms = MultiplexedDuelReader.sauerBytesToParsedMessages(a)
//  i <- if ( ms.isEmpty ) List("Oh, empty, snap") else ms
//  } yield a -> i
//
//  kiki take 20 foreach println
//  gs.flatMap(MultiplexedDuelReader.sauerBytesToParsedMessages) take 50 foreach println

  val A = BetterMultiplexedReader.multiplexSecond(getSauerBytes) map (_.toXml)

  val B = MultiplexedDuelReader.multiplexSecond(getSauerBytes) map (_.toSimpleCompletedDuel.toXml)

  case class RecordedGame(id: String, players: Set[String], frags: Set[String])

  A take 50 foreach println

//
//  val aRecorded = for {
//    g <- A
//    id = g \@ "simple-id"
//    players = (g \\ "player").map(_ \@ "name").toSet
//    frags = (g \\ "player").map(_ \@ "frags").toSet
//  } yield RecordedGame(id, players, frags)
//
//  val bRecorded = for {
//    g <- B
//    id = g \@ "simple-id"
//    players = (g \\ "player").map(_ \@ "name").toSet
//    frags = (g \\ "player").map(_ \@ "frags").toSet
//  } yield RecordedGame(id, players, frags)
//
//  val aSet = aRecorded.dropWhile(_.id startsWith "2014-09-01").takeWhile(_.id startsWith "2014-09-02").toList.map(i => i.id -> i).toMap
//
//  val bSet = bRecorded.dropWhile(_.id startsWith "2014-09-01").takeWhile(_.id startsWith "2014-09-02").toList.map(i => i.id -> i).toMap
//
//  val ids = (aSet.keySet ++ bSet.keySet).toList.sorted
//
//  for {
//    id <- ids
//    a = aSet.get(id)
//    b = bSet.get(id)
//    if a != b
//  }
//  {
//    println(id, a, b)
//  }

}
