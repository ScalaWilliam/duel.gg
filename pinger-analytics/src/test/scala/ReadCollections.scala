import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.{MaterializerSettings, FlowMaterializer}
import akka.util.ByteString
import java.io.File
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.Options
import us.woop.pinger.analytics.processing.Collector
import us.woop.pinger.client.PingPongProcessor
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.persistence.Format._
import us.woop.pinger.data.persistence.Format.Server
import us.woop.pinger.data.persistence.Format.ServerIndexIndexKey
import us.woop.pinger.Extractor

object ReadCollections extends App {

  val target = new File("/home/william/Projects/14/ladder.sauer/indexed-data/15-06")
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
  }

  val exxa = Extractor.extract.lift andThen { (_: Option[Seq[Any]]).toSeq.flatten }

  def listServerData(serverDataStart: Server) = {
    val iter = db.iterator()
    iter.seek(serverDataStart.toBytes)
    val startAt = iter.next().getValue
    iter.seek(startAt)
    iter.asScala.map{
      i => i.getKey -> i.getValue
    }.collect {
      case (DecodeServerDataKey(ServerDataKey(index, `serverDataStart`)), data) =>
        Option(index -> serverDataStart -> ByteString(data))
      case _ => None
    }.takeWhile{_.isDefined}.flatMap{_.toIterable}.collect {
      case ((index, server), data) =>
        for { v <- exxa apply data } yield ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, v)
    }.flatten
  }

  implicit val system = ActorSystem("Sys")
  val materializer = FlowMaterializer(MaterializerSettings())
  val datums = listServers().flatMap(listServerData)
  datums

  def listEverythingPossible(): Iterator[ParsedMessage] = {
    val iter = db.iterator()
    iter.seek(ServerIndexIndexKey().toBytes)
    iter.next()
    iter.seek(iter.next.getValue)
    iter.asScala.map{ i => i.getKey -> i.getValue }.collect{
      case (DecodeServerDataKey(ServerDataKey(index, server)), data) =>
        Option((index -> server) -> ByteString(data))
      case _ => None
    }.takeWhile(_.isDefined).flatMap{_.toIterator}.flatMap{
      case ((index,server), data) =>
      for { v <- exxa apply data} yield ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, v)
    }
  }
  Collector.multiplexFlows(Flow(listEverythingPossible).toProducer(materializer)).map(Collector.processGameData).filter{_.isRight}.map{_.right.get}.take(50).foreach{
//  Collector.multiplexFlows(Flow(datums).toProducer(materializer)).map(Collector.processGameData).foreach{
    println
  }.onComplete(materializer) {
    x =>
      println(s"Done! with $x")
      system.shutdown()
      db.close()
  }
}
