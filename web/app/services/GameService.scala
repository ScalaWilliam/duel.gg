package services

import af.flat.ShowLine
import gg.duel.enricher.GameNode
import gg.duel.enricher.lookup.BasicLookingUp
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future, blocking}

case class Duel(startTime: String, duration: Int, server: String, map: String, mode: String, players: List[DuelPlayer],
                winner: Option[String])

object Duel {

  val duelRender = new ShowLine[Duel] {
    def renders = List(
      "startTime" ~> (_.startTime),
      "duration" ~> (_.duration),
      "map" ~> (_.map),
      "mode" ~> (_.mode),
      "winner" ~> (_.winner)
    )
  }

  val duelPlayerRender = new ShowLine[DuelPlayer] {
    def renders = List(
      "name" ~> (_.name),
      "ip" ~> (_.ip),
      "frags" ~> (_.frags),
      "weapon" ~> (_.weapon),
      "accuracy" ~> (_.accuracy)
    )
  }

  val duelGamePlayerRender: ShowLine[(Duel, DuelPlayer)] =
    duelRender.prefix("duel ").contraMap[(Duel, DuelPlayer)](_._1) &
      duelPlayerRender.prefix("player ").contraMap[(Duel, DuelPlayer)](_._2)

  implicit val jsonReadsp = Json.format[DuelPlayer]
  implicit val jsonReads = Json.format[Duel]
}

case class DuelPlayer(name: String, ip: String, frags: Int, weapon: String, accuracy: Int)

@javax.inject.Singleton
class GameService @javax.inject.Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext) {
  def gamesSource = configuration.underlying.getString("dg.games.source")

  val games: Future[List[Duel]] = Future {
    blocking {
      val src = scala.io.Source.fromFile(gamesSource)
      try src
        .getLines()
        .flatMap {
          _.split("\t").drop(1).headOption
        }
        .map { str =>
          val n = GameNode(jsonString = str, plainGameEnricher = BasicLookingUp.onlyClans)
          n.Mutations.enrich()
          n.asJson
        }
        .filter(_.endsWith(""""tags":["duel"]}"""))
        .map(s => Json.fromJson[Duel](Json.parse(s)).get)
        .toList
      finally src.close()
    }
  }
}
