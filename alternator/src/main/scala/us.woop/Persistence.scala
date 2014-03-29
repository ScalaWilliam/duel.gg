package us.woop

import akka.actor.Actor

import us.woop.Persistence.Replay
import us.woop.PongProcessing.ProcessedMessage
import us.woop.SauerbratenPinger.Ping
import us.woop.SauerbratenPinger.ReceivedBadMessage
import us.woop.SauerbratenPinger.ReceivedMessage
import us.woop.SauerbratenProtocol.Data._
import org.iq80.leveldb.DB

object Persistence {
  case object Replay
  case class Replayed(key: Array[Byte], value: Array[Byte])
  def database = {
    import org.iq80.leveldb.Options
    import java.io.File
    import org.fusesource.leveldbjni.JniDBFactory._

    val options = new Options {
      createIfMissing(true)
    }
    val file = new File("stuff")
    factory.open(file, options)
  }
  implicit class PutObjectImplicit[T](what: T)(implicit db: DB) {

    import scala.pickling._
    import binary._
    def put() {
      val key = what.getClass.getSimpleName + ":" + com.github.nscala_time.time.Imports.DateTime.now.toDateTimeISO.toString
      db.put(key.getBytes, what.pickle.value)
    }
  }
}
class Persistence extends Actor {
  import Persistence._
  implicit val db = database
  def receive = {
    case message: ReceivedMessage =>
      message.put()
    case message: ReceivedBadMessage =>
      message.put()
    case message: Ping =>
      message.put()
    case message @ ProcessedMessage(time, _: PlayerExtInfo) =>
      message.put()
    case message: ProcessedMessage[ServerInfoReply] =>
      message.put()
    case message: ProcessedMessage[PlayerCns] =>
      message.put()
    case message: ProcessedMessage[PlayerExtInfo] =>
      message.put()
    case message: ProcessedMessage[HopmodUptime] =>
      message.put()
    case message: ProcessedMessage[TeamScores] =>
      message.put()
    case message: ProcessedMessage[Uptime] =>
      message.put()
    case message: ProcessedMessage[ThomasExt] =>
      message.put()
    case message: ProcessedMessage[OlderClient] =>
      message.put()
    case Replay =>
      val backTo = sender
      val snapshot = database.getSnapshot
      try {
        val iterator = database.iterator()
        try {
          val values = Stream.continually{iterator.hasNext}.takeWhile(x => x).map{b => iterator.next()}
          values.take(50).foreach {
            x => backTo ! Replayed(x.getKey, x.getValue)
          }
        } finally {
          iterator.close()
        }
      } finally {
        snapshot.close()
      }
  }

  override def postStop() {
    database.close()
  }
}