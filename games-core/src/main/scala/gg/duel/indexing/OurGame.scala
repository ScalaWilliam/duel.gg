package gg.duel.indexing

import java.time.ZonedDateTime

import play.api.libs.json.{Json, JsValue}

/**
 * Created on 04/10/2015.
 */
case class OurGame(`type`: String, endTimeText: ZonedDateTime, players: Option[Map[String, OurPlayer]], teams: Option[Map[String, OurTeam]], clanwar: Option[Set[String]]) {
  def id = endTimeText.toString
}
object OurGame {
  def fromJson(jsValue: JsValue) = Json.fromJson[OurGame](jsValue)
}

case class OurPlayer(name: String, user: Option[String], clan: Option[String])
case class OurTeam(name: String, clan: Option[String])
