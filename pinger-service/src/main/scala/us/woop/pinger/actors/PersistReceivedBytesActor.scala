package us.woop.pinger.actors

import java.io.File

import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import org.iq80.leveldb
import org.iq80.leveldb._
import us.woop.pinger.data.persistence.Format.{ServerDataKey, ServerIndexIndexKey}
import us.woop.pinger.data.persistence.{Format, Persistence}
import us.woop.pinger.service.PingPongProcessor

import scala.util.control.NonFatal


class PersistReceivedBytesActor(target: File) extends Act with ActorLogging {

  import PingPongProcessor.ReceivedBytes

  var db: DB = _

  whenStarting {
    db = Persistence.database(target)
    ensureIndexIndex()
    log.info("Starting LevelDB persister at {}", target)
  }

  def ensureIndexIndex() {
    val indexIndexKey = ServerIndexIndexKey().toBytes
    if ( db.get(indexIndexKey) == null ) {
      db.put(indexIndexKey, Array[Byte]())
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
      case NonFatal(e) => e
    }
  }

  become {
    case msg: ReceivedBytes =>
      persist(msg)
  }
}
