package gg.duel

import java.time.ZonedDateTime

import play.api.libs.json.JsObject

case class SimpleGame(id: String, gameJson: String, server: String, enhancedJson: String, enhancedNativeJson: JsObject, gameType: String,
                     map: String, users: Set[String], clans: Set[String], players: Set[String], tags: Set[String], demo: Option[String]) {
  def dateTime = ZonedDateTime.parse(id)
}
object SimpleGame {
  def stub(id: String) = SimpleGame(
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
