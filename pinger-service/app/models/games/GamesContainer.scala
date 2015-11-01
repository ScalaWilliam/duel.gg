package models.games

import play.api.libs.json.JsValue


/**
 * Created on 13/07/2015.
 */
case class GamesContainer(games: Map[String, JsValue]) {
  def withGame(id: String, json: JsValue) = GamesContainer(games = games + (id -> json))
  def latest(n: Int): List[(String, JsValue)] = games.toList.sortBy(_._1).takeRight(n).toList
}
object GamesContainer {
  def empty = GamesContainer(games = Map.empty)
}

