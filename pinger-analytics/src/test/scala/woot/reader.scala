import java.io.File

import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.Options
import us.woop.pinger.analytics.applications.DatabaseReader
import us.woop.pinger.analytics.processing.DuelMaker.DuelState
import us.woop.pinger.analytics.processing.StreamedDuelMaker._
import us.woop.pinger.data.persistence.Format.Server

object reader extends App {

//  val target = new File("/home/william/Projects/14/ladder.sauer/indexed-data/15-06")
//  val target = new File("/home/william/Projects/14/ladder.sauer/baba/back-up")
  val target = new File("/home/william/Projects/14/ladder.sauer/19-21")
  val db = factory.open(target, new Options())

  val servers = DatabaseReader.listServers(db)

  servers foreach println
//
  val server = Server("188.226.169.46",28785)
  val serverData = DatabaseReader.listServerData(db, server) take 5000

//  System.exit(1)
//  val beebee = ByteString(0, 0, -1, -1, 105, -127, -42, -9, 124, 0)
//
//  val out = Extractor.extract.apply(beebee)
//  println(out)

  trait IteratorState
  case class InDuelState(duelState: DuelState) extends IteratorState
  case object OutOfDuelState extends IteratorState

  import IteratorImplicit._
  val serverDatazz = DatabaseReader.listServerData(db, server).take(90000).asStateIterator

  val collectGames = serverDatazz collect {
    case a: ZFoundGame => new { val finding = Option(a); val rejection = Option.empty[ZRejectedDuelState] }
    case b: ZRejectedDuelState => new { val finding = Option.empty[ZFoundGame]; val rejection = Option(b) }
  }

  val collectedGames = collectGames.toStream
  collectedGames.flatMap(_.rejection.toStream).toVector foreach println
//  collectedGames.flatMap(_.finding.toStream).toVector foreach println
}