package gg.duel.query

import java.time.ZonedDateTime

import play.api.libs.json.JsObject

case class QueryableGame(id: String, gameJson: String, server: String, enhancedJson: String, enhancedNativeJson: JsObject, gameType: String,
                         map: String, users: Set[String], clans: Set[String], players: Set[String], tags: Set[String], demo: Option[String]) {
  def dateTime = ZonedDateTime.parse(id)
}
object QueryableGame {
  def stub(id: String) = QueryableGame(
    id = id,
    gameJson = "{}",
    server = "",
    enhancedJson = "{}",
    enhancedNativeJson = JsObject(Seq.empty),
    gameType = "duel",
    map = "academy",
    users = Set.empty,
    clans = Set.empty,
    players = Set.empty,
    tags = Set.empty,
    demo = Option.empty
  )
}
