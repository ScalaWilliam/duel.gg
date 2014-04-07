package us.woop.contained

import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import us.woop.pinger.client.PingPongProcessor.ParsedMessage
import scala.util.control.NonFatal
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo
import us.woop.pinger.SauerbratenServerData.Conversions.ConvertedServerInfoReply
import us.woop.contained.Persister.DatabaseUseException
import java.io.File

object Persister {
  class DatabaseUseException(e: Throwable) extends RuntimeException(s"Failed to use database because: $e", e)
}

class Persister(target: File) extends Act with ActorLogging {
  import org.iq80.leveldb._
  import org.fusesource.leveldbjni.JniDBFactory._
  import java.io._
  val options = new Options()
  options.createIfMissing(true)


  var db: DB = null
  whenStarting {
    try {
      db = factory.open(target, options)
      db.resumeCompactions()
    } catch {
      case e: Throwable => throw new DatabaseUseException(e)
    }
  }


  whenStopping {
    if ( db != null ) db.close()
  }

  def persist(msg: ParsedMessage) {
    try {
      db.put(s"${msg.host._1}:${msg.host._2}:${System.currentTimeMillis}".getBytes("UTF-8"), msg.recombined.toArray)
    } catch {
      case NonFatal(e) => throw new DatabaseUseException(e)
    }
  }

  become {
    case c @ ParsedMessage(host, _, content: PlayerExtInfo) =>
      persist(c)
    case c @ ParsedMessage(host, _, content: ConvertedServerInfoReply) =>
      persist(c)
  }
}
