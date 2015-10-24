import controllers.{Game, Games}
import play.api.libs.json.{JsObject, JsValue, Writes, Json}

/**
 * Created on 04/10/2015.
 */
package object serializers {
  implicit val gameSerializer: Writes[Game] = new Writes[Game] {
    override def writes(o: Game): JsValue = o.gameJson
  }
  implicit val gamesSerializer: Writes[Games] = new Writes[Games] {
    override def writes(o: Games): JsValue = {
      JsObject(o.games.map{ case (gameId, Game(gameJson, _)) => gameId -> gameJson})
    }
  }
}
