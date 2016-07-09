package services

import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.blocking

@javax.inject.Singleton
class GameService @javax.inject.Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext) {
  def gamesSource = configuration.underlying.getString("dg.games.source")

  val games: Future[List[String]] = Future {
    blocking {
      val src = scala.io.Source.fromFile(gamesSource)
      try src.getLines().take(5).toList
      finally src.close()
    }
  }
}
