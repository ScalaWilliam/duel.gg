import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import models.games._
import models.servers.Servers
import play.api.http.Writeable
import play.api.libs.json._

/**
 * Created on 13/07/2015.
 */
package object controllers {

  implicit val writesServers: Writes[Servers] = new Writes[Servers] {
    override def writes(o: Servers): JsValue =
      JsObject(o.servers.mapValues(_.getAddress).mapValues(JsString.apply))
  }

  implicit val httpWritesServers = implicitly[Writeable[JsValue]].map(Json.toJson(_: Servers))

}
