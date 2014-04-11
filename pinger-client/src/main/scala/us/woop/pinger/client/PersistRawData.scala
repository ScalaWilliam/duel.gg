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
import us.woop.pinger.client.persistence.Format.{ServerDataKey, ServerIndexKey}
import sun.text.resources.FormatData_sk
import us.woop.pinger.client.persistence.Format


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
      target.mkdirs()
      log.info("Opening database {}", target.getAbsoluteFile.getCanonicalPath)
      db = factory.open(target, options)
      val indexIndexKey = ServerIndexKey().toBytes
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

  // TODO make LevelDB into a scala mutable Map!!! :-)
  def ensureFirstIndex(server: Format.Server, key: Array[Byte]) {
    val indexKey = server.toBytes
    if (db.get(indexKey) == null) {
      db.put(indexKey, key)
    }
  }

  def persist(msg: PingPongProcessor.ReceivedBytes) {
    try {
      val sserver = Format.Server(msg.server.ip.ip, msg.server.port)
      val key = ServerDataKey(msg.time, sserver).toBytes
      ensureFirstIndex(sserver, key)
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
