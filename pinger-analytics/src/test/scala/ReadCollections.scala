import akka.util.ByteString
import java.io.File
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.Options
import scalaz.stream.Process
import us.woop.pinger.analytics.processing.Collector
import us.woop.pinger.data.actor.PingPongProcessor
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.persistence.Format._
import us.woop.pinger.data.persistence.Format.Server
import us.woop.pinger.data.persistence.Format.ServerIndexIndexKey
import us.woop.pinger.Extractor

object ReadCollections extends App {
  val target = new File("/home/william/Projects/14/ladder.sauer/indexed-data/12-11")
  val db = factory.open(target, new Options())
  import collection.JavaConverters._
  def listServers() = {
    val iter = db.iterator()


    iter.seek(ServerIndexIndexKey().toBytes)
    assert(iter.next().getKey.toList == ServerIndexIndexKey().toBytes.toList, "There should be a server index index key")

    val servers = iter.asScala.map {
      i => i.getKey -> i.getValue
    }.collect {
      case (ServerIndexKey(server), DecodeServerDataKey(stuff)) => Option(server -> stuff)
      case _ => None
    }.takeWhile {
      _.isDefined
    }.flatMap {
      _.toSeq.map{_._1}
    }
    servers
//    println(servers mkString "\n")
//    iter.close()
  }

  val exxa = Extractor.extract.lift andThen { (_: Option[Seq[Any]]).toSeq.flatten }

  def listServerData(serverDataStart: Server) = {
    val iter = db.iterator()
//    iter.seek(Server("188.226.169.46",28785).toBytes)
    iter.seek(serverDataStart.toBytes)
    val startAt = iter.next().getValue
    iter.seek(startAt)
    val gameData = iter.asScala.map{
      i => i.getKey -> i.getValue
    }.collect {
      case (DecodeServerDataKey(ServerDataKey(index, `serverDataStart`)), data) =>
        Option(index -> serverDataStart -> ByteString(data))
      case _ => None
    }.takeWhile{_.isDefined}.flatMap{_.toIterable}.collect {
      case ((index, server), data) =>
        for { v <- exxa apply data } yield ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, v)
    }.flatten

    val woot = (Process(gameData.toSeq :_*) |> Collector.getGame |> Collector.processGame).flush

//    val cws = woot.par.flatMap{i => Seq(DuelMaker.makeDuel(i), ClanmatchMaker.makeMatch(i))}.toList
//.flatMap{_.right.toSeq}.toList
//
//    val cwss = cws ::: woot.par.map{ClanmatchMaker.makeMatch}.toList
//      .flatMap{_.right.toSeq}.toList
    iter.close()
    woot
  }

//  val allS = listServers().toList
  val allS = List(Server("109.73.51.58",28785))

  println(allS)

  allS.par.flatMap{listServerData}.toIterator foreach println

  db.close()
}
