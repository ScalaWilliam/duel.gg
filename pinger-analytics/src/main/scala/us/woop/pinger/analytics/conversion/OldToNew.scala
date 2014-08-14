package us.woop.pinger.analytics.conversion

import java.io.File
import java.nio.{ByteOrder, ByteBuffer}
import us.woop.pinger.data.Server
import us.woop.pinger.data.log.SauerBytes

object OldToNew {

  case class ServerDataKey(index: Long, server: Server)

  object DecodeServerDataKey {

    def unapply(data: Array[Byte]) =
      decodeKey(data)

    def decodeKey(key: Array[Byte]): Option[ServerDataKey] = {
      for {
        bytes <- Option(key)
        if key.length == 17
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        if bb.get == 2
        ip = {
          val ipB = ByteBuffer.allocate(4)
          bb.get(ipB.array(), 0, 4)
          ipB.array().map {
            _.toInt & 0xFF
          }.mkString(".")
        }
        port = bb.getInt
        idx = bb.getLong
      } yield {
        ServerDataKey(idx, Server(ip, port))
      }
    }
  }

  import org.iq80.leveldb._
  def loadData(db: DB): Iterator[SauerBytes] = {
    import scala.collection.JavaConverters._
    val iterator = db.iterator()
    iterator.seekToFirst()
    iterator.asScala.map(e => e.getKey -> e.getValue).collect {
      case (DecodeServerDataKey(ServerDataKey(index, server)), data) =>
        SauerBytes(server, index, data.toVector)
    }
  }
}
