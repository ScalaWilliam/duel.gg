package us

import java.io.{FileInputStream, File}
import us.woop.pinger.analytics.worse.MultiplexedDuelReader
import us.woop.pinger.data.journal.{SauerBytesWriter, SauerBytesBinary}

import scala.concurrent.Await

object DifferentLoader extends App {

  val ai = new StandaloneWSAPI
  val persister = new WSAsyncDuelPersister(ai, "http://127.0.0.1:2984", "db-stage", "antuipoq")

  val yes = new File("data/newest").listFiles()

  val goog = yes.filter(_.getName endsWith ".log").sortBy(_.getName).toIterator.flatMap { f =>
    println(s"Reading file ${f.getName}")
    val getter = SauerBytesWriter.inputStreamNumBytes(new FileInputStream(f))
    def readMore = SauerBytesWriter.readSauerBytes(getter)
    Iterator.continually(readMore).takeWhile(_.isDefined).map(_.get)
  }

  val duels = MultiplexedDuelReader.multiplexSecond(goog)

  import concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
//  Await.result(persister.createDatabase,2.seconds)
//  duels take 1 foreach println
  duels map {
    d => Await.result(persister.pushDuel(d.toSimpleCompletedDuel.copy(metaId = Option("loading-stage"))), 2.seconds)
  } foreach println
  ai.close()
}
