package gg.duel

import java.time.ZonedDateTime

import play.api.libs.json.JsObject

case class SimpleGame(id: String, gameJson: String, server: String, enhancedJson: String, enhancedNativeJson: JsObject, gameType: String,
                     map: String, users: Set[String], clans: Set[String], players: Set[String], tags: Set[String], demo: Option[String]) {
  def dateTime = ZonedDateTime.parse(id)
}
