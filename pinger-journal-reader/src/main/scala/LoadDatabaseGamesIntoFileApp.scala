import java.io.FileOutputStream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import play.api.libs.json.Json
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

/**
  * Created by William on 09/11/2015.
  */
object LoadDatabaseGamesIntoFileApp extends App {
  val destinationDatabase = Database.forURL(url = args(0), driver = "org.postgresql.Driver")
  val games = TableQuery[Games]

  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val fw = new FileOutputStream("games.txt", false)

  val src = Source(destinationDatabase.stream(games.result))
    .runForeach { case (id, json) =>
      val outputJson = Json.parse(json).toString()
      require(!outputJson.contains("\n"), s"JSON should not contain newlines, this id $id did: $outputJson")
      val outputLine = s"$id\t$outputJson\n"
      fw.write(outputLine.getBytes("UTF-8"))
    }

  src.onComplete { case result =>
    println("ALL DONE.")
    fw.close()
  }

}
