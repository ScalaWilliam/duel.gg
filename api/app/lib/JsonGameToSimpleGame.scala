package lib

import de.heikoseeberger.akkasse.ServerSentEvent
import gg.duel.SimpleGame
import gg.duel.enricher.GameNode
import gg.duel.enricher.lookup.LookingUp
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

object JsonGameToSimpleGame {

  def theCatch: PartialFunction[Throwable, Nothing] = {
    case x: Throwable =>
      Logger.error("K, inside enricher loop this problem happened", x)
      throw x
  }
}
case class JsonGameToSimpleGame(enricher: LookingUp) {
  def apply(json: String): Option[SimpleGame] = {
    val gn = GameNode(jsonString = json, plainGameEnricher = enricher)
    gn.enrich()
    Option {
      SimpleGame(
        id = gn.startTimeText.get,
        gameJson = json,
        server = gn.server.get,
        enhancedJson = gn.asJson,
        enhancedNativeJson = Json.parse(gn.asPrettyJson).asInstanceOf[JsObject],
        gameType = gn.gameType.get,
        users = Set.empty,
        clans = gn.allPlayers.flatMap(_.clan).toSet,
        players = gn.allPlayers.flatMap(_.name).toSet,
        tags = gn.tags,
        demo = gn.demo,
        map = gn.map.get
      )
    }
  }
  def apply(sse: ServerSentEvent): Option[SimpleGame] = {
    sse.id.flatMap { id => apply(json = sse.data) }
  }

  import akka.stream.scaladsl._

  def createFlow: Flow[ServerSentEvent, SimpleGame, Unit] = {
    Flow.apply[ServerSentEvent].mapConcat(sse => try apply(sse).toList catch JsonGameToSimpleGame.theCatch)
  }

}