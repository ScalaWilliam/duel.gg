import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import models.games.{GamesIndex, Duels, Ctfs, Games}
import models.servers.Servers
import play.api.http.Writeable
import play.api.libs.json._

/**
 * Created on 13/07/2015.
 */
package object controllers {

  implicit val writesSCC: Writes[SimpleCompletedCTF] = new Writes[SimpleCompletedCTF] {
    override def writes(o: SimpleCompletedCTF): JsValue = Json.parse(o.toPrettyJson)
  }
  implicit val writesSCD: Writes[SimpleCompletedDuel] = new Writes[SimpleCompletedDuel] {
    override def writes(o: SimpleCompletedDuel): JsValue = Json.parse(o.toPrettyJson)
  }
  implicit val writesServers: Writes[Servers] = new Writes[Servers] {
    override def writes(o: Servers): JsValue =
      JsObject(o.servers.mapValues(_.getAddress).mapValues(JsString.apply))
  }

  implicit val writesGames = Json.writes[Games]
  implicit val writesGamesIndex = Json.writes[GamesIndex]
  implicit val writesCtfs = Json.writes[Ctfs]
  implicit val writesDuels = Json.writes[Duels]

  implicit val httpWritesServers = implicitly[Writeable[JsValue]].map(Json.toJson(_: Servers))

  implicit val httpWritesGames = implicitly[Writeable[JsValue]].map(Json.toJson(_: Games))
  implicit val httpWritesGamesIndex = implicitly[Writeable[JsValue]].map(Json.toJson(_: GamesIndex))
  implicit val httpWritesCtfs = implicitly[Writeable[JsValue]].map(Json.toJson(_: Ctfs))
  implicit val httpWritesDuels = implicitly[Writeable[JsValue]].map(Json.toJson(_: Duels))

}