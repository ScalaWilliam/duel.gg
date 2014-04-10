package us.woop.pinger.client

import java.io.File
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import scala.util.control.NonFatal
import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.nio.ByteOrder
import org.iq80.leveldb
import us.woop.pinger.data.PingPongProcessor.Server


class PersistRawData(target: File) extends Act with ActorLogging {

  import us.woop.pinger.data.{PingPongProcessor, PersistRawData}
  import PersistRawData.DatabaseUseException
  import akka.util.ByteStringBuilder
  import PingPongProcessor.ReceivedBytes
  var db: DB = _

  whenStarting {
    try {
      val options = {
        val options = new Options()
        options.createIfMissing(true)
      }
      log.info("Opening database {}", target)
      db = factory.open(target, options)
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      val indexIndexKey = new ByteStringBuilder().putBytes("server.index".getBytes).putLong(0L).result().toArray
      if ( db.get(indexIndexKey) == null ) {
        db.put(indexIndexKey, Array[Byte]())
      }
    } catch {
      case NonFatal(e) =>
        throw new DatabaseUseException(e)
    }
  }

  whenStopping {
    if ( db != null ) {
      db.close()
      log.info("Database {} closed", target)
    }
  }

  val wo = new leveldb.WriteOptions { sync(true) }

  def ensureFirstIndex(server: Server, key: Array[Byte]) {
    val ipBytes = server.ip.ip.split("\\.").map{_.toInt.toByte}
    implicit val byteOrdering = ByteOrder.BIG_ENDIAN
    val indexKey = new ByteStringBuilder().putBytes("server.index".getBytes).putBytes(ipBytes).putInt(server.port).result().toArray
    // TODO make LevelDB into a scala mutable Map!!! :-)
    if (db.get(indexKey) == null) {
      db.put(indexKey, key)
    }
  }

  def persist(msg: PingPongProcessor.ReceivedBytes) {
    try {
      val key = {
        val ipBytes = msg.server.ip.ip.split("\\.").map{_.toInt.toByte}
        implicit val byteOrdering = ByteOrder.BIG_ENDIAN
        new ByteStringBuilder().putBytes("msg".getBytes).putBytes(ipBytes).putInt(msg.server.port).putLong(msg.time).result().toArray
      }
      ensureFirstIndex(msg.server, key)
      db.put(key, msg.message.toArray, wo)
    } catch {
      case NonFatal(e) => throw new DatabaseUseException(e)
    }
  }

  become {
    case msg: ReceivedBytes =>
      persist(msg)
  }
}
