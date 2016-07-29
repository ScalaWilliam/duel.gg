package gg.duel.pinger

import gg.duel.pinger.data.journal.JournalReader

object ReaderApp extends App {

  val fah = new java.net.URL(args(1))

  val br = new JournalReader(fah)
  try {
//    println(br.getGamesIterator.map(_.toJson).toStream.size)
//        br.getGamesIterator.map(_.toJson).foreach(println)
        println(br.getGamesIterator.count(_ => true)) //.foreach(println)
  } finally br.close()

}
