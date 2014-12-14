package us

import java.io.{File, FileInputStream}

import us.woop.pinger.analytics.better.BetterMultiplexedReader
import us.woop.pinger.data.journal.SauerBytesWriter

object DifferentLoader extends App {

  val ai = new StandaloneWSAPI
  val persister = new WSAsyncGamePersister(ai, "http://127.0.0.1:2984", "db-stage", "antuipoq")

//  val yes = new File("data/newest").listFiles()

//  val goog = yes.filter(_.getName endsWith ".log").sortBy(_.getName).

  val goog = List(new File("data/sb-sb-20140929-0656-e0ff18ed.log")).
    toIterator.flatMap { f =>
    println(s"Reading file ${f.getName}")
    val getter = SauerBytesWriter.inputStreamNumBytes(new FileInputStream(f))
    def readMore = SauerBytesWriter.readSauerBytes(getter)
    Iterator.continually(readMore).takeWhile(_.isDefined).map(_.get)
  }

  val duels = BetterMultiplexedReader.multiplexSecond(goog)

//  duels take 5 foreach println
  duels flatMap (_.game.right.toSeq) map(_.toXml) take 5 foreach println

  ai.close()
//  Await.result(persister.createDatabase,2.seconds)
//  duels take 1 foreach println
//  duels map {
//    d => Await.result(persister.pushDuel(d.toSimpleCompletedDuel.copy(metaId = Option("loading-stage"))), 2.seconds)
//  } foreach println
//  ai.close()
}
