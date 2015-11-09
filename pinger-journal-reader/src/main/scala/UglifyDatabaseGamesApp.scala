
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import gg.duel.pinger.data.journal.JournalReader
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

object UglifyDatabaseGamesApp extends App {
  val destinationDatabase = Database.forURL(url = args(0), driver = "org.postgresql.Driver")
  val games = TableQuery[Games]

  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val src = Source(destinationDatabase.stream(games.result))
    .mapConcat { case (id, json) =>
      val newJson = Json.parse(json).toString()
      if ( newJson == json ) List.empty else List(id -> games.insertOrUpdate(id, newJson))
    }
    .map { case (id, req) => id -> DBIO.seq(req) }
    .mapAsync(1) { case (id, s) =>
      destinationDatabase.run(s)
    }
  src.runForeach(println).onComplete { case result =>

    println("ALL DONE.")
  }

}