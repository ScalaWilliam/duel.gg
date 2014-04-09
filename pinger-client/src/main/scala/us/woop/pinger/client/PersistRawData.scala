package us.woop.pinger.client

import java.io.File
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import scala.util.control.NonFatal
import us.woop.pinger.data.{PingPongProcessor, PersistRawData}
import PersistRawData.DatabaseUseException
import akka.util.ByteStringBuilder
import PingPongProcessor.ReceivedBytes
import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.nio.ByteOrder
import org.iq80.leveldb



class PersistRawData(target: File) extends Act with ActorLogging {

  var db: DB = _

  whenStarting {
    try {
      val options = {
        val options = new Options()
        options.createIfMissing(true)
      }
      db = factory.open(target, options)
      db.resumeCompactions()
    } catch {
      case NonFatal(e) =>
        throw new DatabaseUseException(e)
    }
  }

  whenStopping {
    if ( db != null ) {
      db.close()
    }
  }

  val wo = new leveldb.WriteOptions { sync(true) }

  def persist(msg: PingPongProcessor.ReceivedBytes) {
    try {
      val key = {
        val ipBytes = msg.server.ip.ip.split("\\.").map{_.toInt.toByte}
        implicit val byteOrdering = ByteOrder.LITTLE_ENDIAN
        new ByteStringBuilder().putBytes(ipBytes).putInt(msg.server.port).putLong(msg.time).result().toArray
      }
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
