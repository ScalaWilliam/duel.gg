package gg.duel.pinger.app

import java.io.{File, FileInputStream}

import gg.duel.pinger.analytics.MultiplexedReader
import gg.duel.pinger.service.{StandaloneWSAPI, WSAsyncGamePersister}
import gg.duel.pinger.data.journal.SauerBytesWriter

import scala.util.Try

object ReplayOlderStuffApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  val fah = new File("/home/william/Projects/37/ladder.sauer/data/sb-sb-20140929-0656-e0ff18ed.log")
  val is = new FileInputStream(fah)
//  is.skip(4973401493L)
  val get = SauerBytesWriter.inputStreamNumBytes(is)

  def findReadablePosition(): Long = {
    val originalPosition = is.getChannel.position()
    val sauerBytesRead = Try(SauerBytesWriter.readSauerBytes(get).map(MultiplexedReader.sauerBytesToParsedMessages).toList.flatten).toOption.toList.flatten
    val newPosition = is.getChannel.position()
    if ( sauerBytesRead.isEmpty ) {
      is.getChannel.position(originalPosition + 1)
      findReadablePosition()
    } else {
      originalPosition
    }
  }

  def snapToNextReadablePosition(): Unit = {
    is.getChannel.position(findReadablePosition())
  }

//  is.getChannel.position(4000000000L)
//  snapToNextReadablePosition()

  val faf = Iterator.continually(SauerBytesWriter.readSauerBytes(get)).takeWhile(_.isDefined).map(_.get)
//
  val persister = new WSAsyncGamePersister(
    client = new StandaloneWSAPI,
    basexContextPath = System.getProperty("pinger.basex.context", "http://prod-b.duel.gg:8984"),
    dbName = System.getProperty("pinger.basex.name", "db-stage"),
    chars = System.getProperty("pinger.chars", "pqnduwaohk")
  )
  val duels = MultiplexedReader.multiplexSecond(faf).map(_.copy(metaId = Option("manual-redo")))
//  println(duels.next)
  duels.foreach {
    item =>
      item.game.left.foreach(i =>
      persister.pushDuel(i).onComplete { x => println(x) }
      )
      item.game.right.foreach(i =>
      persister.pushCtf(i).onComplete { x => println(x) }
      )
  }

//  val first = BetterMultiplexedReader.multiplexSecond(faf).map(_.copy(metaId = Option("manual-redo"))).take(1).toList.headOption.get
//  persister.pushDuel(first).onComplete{ x => {
//
//    println(x)
//    System.exit(0)
//  }}
  //  val faf = Iterator.continually(SauerBytesWriter.readSauerBytes(get)).takeWhile(_.isDefined).map(_.get)
  ////  println(faf.drop(50000000).next())
  ////  val duelsB = BetterMultiplexedReader.multiplexSecond(faf).take(1)
  //  val states = faf.scanLeft(SInitial: SIteratorState)(_.next(_)).flatMap(_.mIteratorState.lastUpdatedState.toIterator)
  //  .filter(_._1.ip.ip == "108.61.210.80").drop(800).drop(500).drop(500).drop(50).take(50)
  ////  sasa foreach println
  ////  states.zipWithIndex foreach println
  ////  foreach println
  ////  println(is.getChannel.position())
  //states.collect {
  //  case SFoundGame(_, completedDuel) => completedDuel
  //}.take(1).map(_.toXml) foreach println
  ////  BetterMultiplexedReader.multiplexSecond(faf).take(1) map (_.toXml) foreach println

}

