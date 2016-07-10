package gg.duel.pinger

import gg.duel.pinger.data.journal.JournalReader

object ReaderApp extends App {

  val fah = new java.net.URL(args(1))

  val br = new JournalReader(fah)
  try {
    val start = System.currentTimeMillis()
    br.getGamesIterator.map(_.toJson).foreach(println)
    val end = System.currentTimeMillis()
  } finally br.close()

}
