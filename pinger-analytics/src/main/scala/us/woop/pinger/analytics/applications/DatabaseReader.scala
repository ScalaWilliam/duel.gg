package us.woop.pinger.analytics.applications

import us.woop.pinger.data.persistence.Format._

import collection.JavaConverters._
import us.woop.pinger.data.persistence.Format.Server
import us.woop.pinger.data.persistence.Format.ServerIndexIndexKey
import akka.util.ByteString
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import org.iq80.leveldb.{DBIterator, DB}
import us.woop.pinger.{PingPongProcessor, Extractor}

object DatabaseReader {
  
  val exxa = Extractor.extract.lift andThen { (_: Option[Seq[Any]]).toSeq.flatten }

  def listServers(db: DB) = {
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

  def getParsedMessages(iterator: DBIterator, onlyServer: Server): Iterator[ParsedMessage] = {
    iterator.asScala.map{ i => i.getKey -> i.getValue }.collect{
      case (DecodeServerDataKey(ServerDataKey(index, `onlyServer`)), data) =>
        Option((index -> onlyServer) -> ByteString(data))
      case _ =>
        None
    }.takeWhile(_.isDefined).flatMap{_.toIterator}.flatMap{
      case ((index,server), data) =>
        val one = for { v <- exxa apply data } yield ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, v)
        List(ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, data -> one))
    }
  }
  def getParsedMessages(iterator: DBIterator): Iterator[ParsedMessage] = {
    iterator.asScala.map{ i => i.getKey -> i.getValue }.collect{
      case (DecodeServerDataKey(ServerDataKey(index, server)), data) =>
            Option((index -> server) -> ByteString(data))
      case _ => None
    }.takeWhile(_.isDefined).flatMap{_.toIterator}.flatMap{
      case ((index,server), data) =>
        for { v <- exxa apply data } yield ParsedMessage(PingPongProcessor.Server(server.ip, server.port), index, v)
    }
  }

  def listEverythingPossible(db: DB): Iterator[ParsedMessage] = {
    val iter = db.iterator()
    iter.seek(ServerIndexIndexKey().toBytes)
    iter.next()
    iter.seek(iter.next.getValue)
    getParsedMessages(iter)
  }
  
  def listServerData(db: DB, serverDataStart: Server) = {
    val iter = db.iterator()
    iter.seek(serverDataStart.toBytes)
    val startAt = iter.next().getValue
    iter.seek(startAt)
    getParsedMessages(iter, serverDataStart)
  }

}
