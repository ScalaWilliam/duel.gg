package us.woop

import akka.actor.Actor

import us.woop.Persistence.Replay
import us.woop.PongProcessing.ProcessedMessage
import us.woop.SauerbratenPinger.Ping
import us.woop.SauerbratenPinger.ReceivedBadMessage
import us.woop.SauerbratenPinger.ReceivedMessage
import us.woop.SauerbratenProtocol._
import us.woop.SauerbratenProtocol.HopmodUptime
import us.woop.SauerbratenProtocol.PlayerCns
import us.woop.SauerbratenProtocol.PlayerExtInfo
import us.woop.SauerbratenProtocol.ServerInfoReply

object Persistence {
  case object Replay
  case class Replayed(key: Array[Byte], value: Array[Byte])
}
class Persistence extends Actor {

  import org.iq80.leveldb.Options
  import java.io.File
  import org.fusesource.leveldbjni.JniDBFactory._

  val database = {
    val options = new Options {
      createIfMissing(true)
    }
    val file = new File("stuff")
    factory.open(file, options)
  }

  def put[T](what: T) {
    import scala.pickling._
    import binary._
    val key = what.getClass.getSimpleName + ":" + com.github.nscala_time.time.Imports.DateTime.now.toDateTimeISO.toString
    database.put(key.getBytes, what.pickle.value)
  }

  def receive = {
    case message: ReceivedMessage =>
      put(message)
    case message: ReceivedBadMessage =>
      put(message)
    case ping: Ping =>
      put(ping)
    case message: ProcessedMessage[PlayerExtInfo] =>
      put(message)
    case message: ProcessedMessage[ServerInfoReply] =>
      put(message)
    case message: ProcessedMessage[PlayerCns] =>
      put(message)
    case message: ProcessedMessage[PlayerExtInfo] =>
      put(message)
    case message: ProcessedMessage[HopmodUptime] =>
      put(message)
    case message: ProcessedMessage[TeamScores] =>
      put(message)
    case message: ProcessedMessage[Uptime] =>
      put(message)
    case message: ProcessedMessage[ThomasExt] =>
      put(message)
    case message: ProcessedMessage[OlderClient] =>
      put(message)
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