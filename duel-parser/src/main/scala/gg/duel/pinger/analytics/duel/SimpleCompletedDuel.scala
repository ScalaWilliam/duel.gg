package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import play.api.libs.json.{JsObject, Json}

object SimpleCompletedDuel {
  import SimpleCompletedCTF.iifmt
  implicit val formats2 = Json.format[SimplePlayerStatistics]
  implicit val formats = Json.format[SimpleCompletedDuel]
  def fromPrettyJson(json: String): SimpleCompletedDuel = {
    Json.fromJson[SimpleCompletedDuel](Json.parse(json)).get
  }
}
case class SimpleCompletedDuel
(
  simpleId: String,
  duration: Int,
  playedAt: List[Int],
  startTimeText: String,
  startTime: Long,
  map: String,
  mode: String,
  serverDescription: String,
  server: String,
  players: Map[String, SimplePlayerStatistics],
  winner: Option[String], metaId: Option[String]) {
  def toPrettyJson = {
    import SimpleCompletedDuel._
    Json.prettyPrint(Json.toJson(this))
  }
  def toPlayJson = Json.toJson(this).asInstanceOf[JsObject]
  def toJson = {
    s"${Json.toJson(this)}"
  }

}