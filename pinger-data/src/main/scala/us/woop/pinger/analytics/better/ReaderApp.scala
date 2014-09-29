package us.woop.pinger.analytics.better

import java.io.{File, FileInputStream}

import us.woop.pinger.analytics.worse.MultiplexedDuelReader
import us.woop.pinger.data.journal.SauerBytesWriter

object ReaderApp extends App {
  val fah = new File("/home/william/Projects/37/ladder.sauer/data/sb-sb-20140901-1121-87b1cd20.log")
  val get = SauerBytesWriter.inputStreamNumBytes(new FileInputStream(fah))
  val faf = Iterator.continually(SauerBytesWriter.readSauerBytes(get)).takeWhile(_.isDefined).map(_.get)
  val duelsB = BetterMultiplexedReader.multiplexSecond(faf).take(5)
  duelsB map (_.toXml) foreach println
}
