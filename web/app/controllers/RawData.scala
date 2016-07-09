package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Source
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{Duel, GameService}

import scala.concurrent.ExecutionContext

/**
  * Created by me on 09/07/2016.
  */
class RawData @Inject()(gameService: GameService)(implicit executionContext: ExecutionContext) extends Controller {

  val TSV_CONTENT_TYPE = "text/tab-separated-values"
  val NEW_LINE = "\r\n"

  val TAB = "\t"

  def gameToPlayerLines(g: Duel): List[String] =
    g.players.map(t => Duel.duelGamePlayerRender.renders.map(_._2.apply(g -> t)).mkString(TAB))

  def asTsv = Action.async {
    val header = Duel.duelGamePlayerRender.renders.map(_._1).mkString(TAB)

    gameService.games.map { gms =>
      val src = Source.single(header) ++ Source(gms).mapConcat(gameToPlayerLines)
      Ok.chunked(src.map(_ + NEW_LINE)).as(TSV_CONTENT_TYPE)

    }
  }

  def asJson = Action.async {
    gameService.games.map(gms =>
      Ok.chunked(Source(gms).map(d => Json.toJson(d).toString).map(_ + NEW_LINE)))
  }
}
