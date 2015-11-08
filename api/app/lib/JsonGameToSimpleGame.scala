package lib

import de.heikoseeberger.akkasse.ServerSentEvent
import gg.duel.enricher.GameNode
import gg.duel.enricher.lookup.LookingUp
import gg.duel.query.QueryableGame
import play.api.libs.json.{JsObject, Json}

case class JsonGameToSimpleGame(enricher: LookingUp) {
  def apply(json: String): Option[QueryableGame] = {
    val gn = GameNode(jsonString = json, plainGameEnricher = enricher)
    gn.enrich()
    Option {
      QueryableGame(
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
  def apply(sse: ServerSentEvent): Option[QueryableGame] = {
    sse.id.flatMap { id => apply(json = sse.data) }
  }

}