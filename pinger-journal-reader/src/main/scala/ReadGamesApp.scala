import java.net.URL

import gg.duel.pinger.data.journal.JournalReader
import slick.driver.PostgresDriver.api._

import scala.async.Async
import scala.concurrent.Future

object ReadGamesApp extends App {

  val destinationDatabase = Database.forURL(url = args(0), driver = "org.postgresql.Driver")
  val games = TableQuery[Games]

  import scala.concurrent.ExecutionContext.Implicits.global

  args.tail.par.foreach { file =>
    val sourceUrl = new URL(file)
    val journalReader = new JournalReader(sourceUrl)
    Async.async {
      Async.await {
        Future.sequence {
          journalReader.getGamesIterator.map { game =>
            val id = game.fold(_.startTimeText, _.startTimeText)
            val json = game.fold(_.toJson, _.toJson)
            val req = DBIO.seq(games.insertOrUpdate(id, json))
            Async.async {
              println(s"Found game ID $id...")
              Async.await(destinationDatabase.run(req))
              println(s"Saved game ID $id.")
            }
          }
        }
      }
    }
    journalReader.close()
  }
}

class Games(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}