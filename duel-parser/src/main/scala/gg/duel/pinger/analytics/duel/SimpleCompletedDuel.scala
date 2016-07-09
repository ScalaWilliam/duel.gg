package gg.duel.pinger.analytics.duel

import play.api.libs.json._

object SimpleCompletedDuel {
  implicit val intIntFormat: Format[(Int, Int)] = Format(
    Reads[(Int, Int)] {
      jsv =>
        val a = jsv \ "_1"
        val b = jsv \ "_2"
        a.get
        (a, b) match {
          case (JsDefined(JsNumber(av)), JsDefined(JsNumber(bv))) => JsSuccess(av.toInt -> bv.toInt)
          case other => JsError(s"Expected _1, and _2, got ${jsv}")
        }
    },
    Writes[(Int, Int)] { case (x, y) => JsObject(Map("_1" -> JsNumber(x), "_2" -> JsNumber(y))) }
  )
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
