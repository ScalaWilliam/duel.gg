package gg.duel.pinger.analytics.duel

import java.io.{File, FileInputStream}
import gg.duel.pinger.analytics.MultiplexedReader
import MultiplexedReader.{SFoundGame, SIteratorState, SInitial}
import gg.duel.pinger.data.journal.SauerBytesWriter

import scala.util.Try

object ReaderApp extends App {

  val fah = new File("***REMOVED***/Projects/37/ladder.sauer/data/sb-sb-20140929-0656-e0ff18ed.log")

  val is = new FileInputStream(fah)
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

//  val positionAtFifth = (sauerBytes)
//  is.skip(4973401493L)
//  is.getChannel.position(900002L)
//  val goodPosition = findReadablePosition()
//  is.getChannel.position(goodPosition)
//  println(goodPosition)

//  val faf = Iterator.continually(SauerBytesWriter.readSauerBytes(get)).takeWhile(_.isDefined).map(_.get)
//  println(faf.drop(50000000).next())
//
//  is.getChannel.size()
//
//  println(is.getChannel.position())
//  BetterMultiplexedReader.multiplexSecond(faf).take(1) map (_.toXml) foreach println
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
