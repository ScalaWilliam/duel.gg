package gg.duel.pinger

import java.nio.file.{Files, Paths}

import gg.duel.pinger.data.journal.JournalReader

object ReaderApp extends App {

  val fah = args.map(arg => Paths.get(arg)).find(p => Files.exists(p)).getOrElse {
    throw new IllegalArgumentException(s"Could not find a file that exists from arguments ${args.toList}")
  }

  val br = new JournalReader(fah.toFile)
  try {
    val start = System.currentTimeMillis()
    br.getGamesIterator.map(_.toJson).foreach(println)
    val end = System.currentTimeMillis()
  } finally br.close()

}
