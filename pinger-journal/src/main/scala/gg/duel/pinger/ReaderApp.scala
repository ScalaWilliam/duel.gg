package gg.duel.pinger

import java.nio.file.{Files, Paths}

import gg.duel.pinger.data.journal.{FilterJournalReader, JournalReader}

object ReaderApp extends App {

  val fah = args.map(arg => Paths.get(arg)).find(p => Files.exists(p)).getOrElse {
    throw new IllegalArgumentException(s"Could not find a file that exists from arguments ${args.toList}")
  }

  val br = new JournalReader(fah.toFile) //, KeepRecord.readFromFile(KeepRecord.pathFor(fah)).servers)
  try {
    val start = System.currentTimeMillis()
    br.getGamesIterator.take(100).foreach(println)
    val end = System.currentTimeMillis()
    println((end - start)/1000)
  } finally br.close()

}
// 63 & 229
