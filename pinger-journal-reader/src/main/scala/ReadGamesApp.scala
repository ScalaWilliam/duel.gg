import java.net.URL

import gg.duel.pinger.data.journal.JournalReader
import slick.driver.H2Driver.api._

import scala.async.Async

object ReadGamesApp extends App {
  val sourceUrl = new URL(args(0))
  val destinationDatabase = Database.forURL(url = args(1), driver="org.h2.Driver")
  val journalReader = new JournalReader(sourceUrl)
  val games = TableQuery[Games]
  val setup = DBIO.seq(games.schema.create)
  import scala.concurrent.ExecutionContext.Implicits.global
  Async.async {
    println("Trying to save dem stuff.")
    Async.await(destinationDatabase.run(setup))
    println("Starting to read games...")
    journalReader.getGamesIterator.foreach{game =>
      val id = game.fold(_.startTimeText, _.startTimeText)
      val json = game.fold(_.toJson, _.toJson)
      val req = DBIO.seq(games += (id, json))
      Async.async {
        println(s"Found game ID $id...")
        Async.await(destinationDatabase.run(req))
        println(s"Saved game ID $id.")
      }
    }
  }
}
class Games(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)
  def json = column[String]("JSON")
  def * = (id, json)
}