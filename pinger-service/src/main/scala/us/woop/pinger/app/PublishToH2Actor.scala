package us.woop.pinger.app

import java.io.{OutputStream, FileOutputStream, FileWriter, File}
import java.nio.{ByteOrder, ByteBuffer}

import akka.actor.ActorDSL._
import akka.util.ByteStringBuilder
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.compress.compressors.{CompressorOutputStream, CompressorStreamFactory}
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.h2.mvstore.`type`.DataType
import org.h2.mvstore.{WriteBuffer, MVMap, MVStore}
import us.woop.pinger.service.PingPongProcessor.{SerializableBytes, ReceivedBytes}

class PublishToH2Actor extends Act {

  var currentStore: MVStore = _

  case object Rotate

  def newUUID = java.util.UUID.randomUUID().toString

  def startWriting(): Unit = {
    val fn = new File(s"db.$newUUID.db")
    val fo = new FileOutputStream(fn)
    fo.write(Array[Byte](5,4,3,2,1,2,3,4,5))
    become(writing(fo))
  }

  whenStarting {
    startWriting()
    import concurrent.duration._
    import context.dispatcher
    context.system.scheduler.schedule(1.day, 1.day, self, Rotate)
  }

  def writing(to: OutputStream): Receive = {
    case Rotate =>
      to.flush()
      to.close()
      startWriting()
    case m @ ReceivedBytes(server, time, message) =>
      val serialized = m.toBytes.take(Integer.MAX_VALUE)
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      val byteArray = new ByteStringBuilder().putInt(serialized.size).putBytes(serialized).result().toArray
      to.write(byteArray)
      to.flush()
  }

  whenStopping {
    if ( currentStore != null ) {
      currentStore.close()
    }
  }

}
