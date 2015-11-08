import java.io.File
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import gg.duel.pinger.data.journal.JournalReader
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.util.Failure

object ReadGamesApp extends App {

  val file = new File(args(1))
//    .listFiles()
//    .collect { case f if f.getName.endsWith("sblog.gz") =>
//      f.getCanonicalFile
//    }.toList

//  files foreach (f => println(s"File found: $f"))

  val destinationDatabase = Database.forURL(url = args(0), driver = "org.postgresql.Driver")
  val games = TableQuery[Games]

  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

//  val parSeqFutureList = files.par.map { file =>
    println(s"Reading journal for file $file")
    val journalReader = new JournalReader(file)
    val journalFinishedF = Source.apply(() => journalReader.getGamesIterator)
      .map { game =>
        val id = game.fold(_.startTimeText, _.startTimeText)
        val json = game.fold(_.toJson, _.toJson)
        id -> games.insertOrUpdate(id, json)
      }
      .map { case (id, req) =>
        id -> DBIO.seq(req)
      }
      .mapAsync(1) { case (id, s) =>
        val ff = destinationDatabase.run(s)
        ff.onComplete {
          case Failure(r) =>
            println(s"At $id $s:")
            r.printStackTrace()
          case r =>
           println(s"Putting action done for game $id: $r")

        }
        ff.map(r => id -> r).recover{case _ => println("K")}
      }.runForeach { _ => () }
    journalFinishedF.onComplete { case r =>
      println(s"Finished journal for $file")
      journalReader.close()
    }
    journalFinishedF
//  }
//
//  val he = Future.sequence(parSeqFutureList.toList)
//  val dontQuit = new CountDownLatch(1)
//  he.onComplete {
//    case r => println(s"Completed everything with result $r")
//      dontQuit.countDown()
//  }
//
//  dontQuit.await()
//
//  println("All done. Bye!")

}

class Games(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}