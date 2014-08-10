package us.woop.pinger.app
import akka.actor.ActorDSL._
import org.h2.mvstore.{MVMap, MVStore}
import us.woop.pinger.service.PingPongProcessor.{SerializableBytes, ReceivedBytes}

class PublishToH2Actor extends Act {

  var currentStore: MVStore = _

  case object Rotate

  def newUUID = java.util.UUID.randomUUID().toString

  def startWriting(): Unit = {
    val newStore = new MVStore.Builder().compressHigh().fileName(newUUID).open()
    val newMap = newStore.openMap[Long, SerializableBytes]("receivedBytes")

    currentStore = newStore
    become(writing(newStore, newMap))
  }

  whenStarting {
    startWriting()
    import concurrent.duration._
    import context.dispatcher
    context.system.scheduler.schedule(5.seconds, 1.hour, self, Rotate)
  }

  def writing(to: MVStore, map: MVMap[Long, SerializableBytes]): Receive = {
    case Rotate =>
      to.commit()
      to.close()
      startWriting()
    case m @ ReceivedBytes(server, time, message) =>
      map.put(map.sizeAsLong() + 1, m.toSerializable)
      to.commit()
  }

  whenStopping {
    if ( currentStore != null ) {
      currentStore.close()
    }
  }

}
