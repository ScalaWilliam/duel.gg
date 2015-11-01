package modules

import de.heikoseeberger.akkasse.ServerSentEvent
import gcc.enrichment.Enricher
import gcc.game.GameReader
import gg.duel.SimpleGame
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

case class JsonGameToSimpleGame(enricher: Enricher, gameReader: GameReader) {
  def apply(id: String, json: String): Option[SimpleGame] = {
    val richJson = enricher.enrichJsonGame(json)
    val richNativeJson = Json.parse(richJson).asInstanceOf[JsObject]
    val gameType = (richNativeJson \ "type").get.as[String]
    val server = (richNativeJson \ "server").get.as[String]
    import collection.JavaConverters._
    Option {
      SimpleGame(
        id = id,
        gameJson = json,
        server = server,
        enhancedJson = richJson,
        enhancedNativeJson = richNativeJson,
        gameType = gameType,
        users = gameReader.getUsers(richJson).asScala.collect { case x: String => x }.toSet,
        clans = gameReader.getClans(richJson).asScala.collect { case x: String => x }.toSet,
        players = gameReader.getPlayers(richJson).asScala.collect { case x: String => x }.toSet,
        tags = gameReader.getTags(richJson).asScala.collect { case x: String => x }.toSet,
        demo = (richNativeJson \ "demo").asOpt[String],
        map = (richNativeJson \ "map").get.as[String]
      )
    }
  }
  def apply(sse: ServerSentEvent): Option[SimpleGame] = {
    sse.id.flatMap { id => apply(id = id, json = sse.data) }
  }

  import akka.stream.scaladsl._

  def createFlow: Flow[ServerSentEvent, SimpleGame, Unit] = {
    Flow.apply[ServerSentEvent].mapConcat(sse => try apply(sse).toList catch JsonGameToSimpleGame.theCatch)
  }

}
object JsonGameToSimpleGame {

  def theCatch: PartialFunction[Throwable, Nothing] = {
    case x: Throwable =>
      Logger.error("K, inside enricher loop this problem happened", x)
      throw x
  }
}