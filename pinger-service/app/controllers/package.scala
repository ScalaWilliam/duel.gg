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

  def addsType(value: String) = __.json.update((__ \ 'type).json.put(JsString(value)))
  def removesSimpleId = (__ \ 'simpleId).json.prune

  implicit val writesSCC: Writes[SimpleCompletedCTF] = new Writes[SimpleCompletedCTF] {
    override def writes(o: SimpleCompletedCTF): JsValue =
      Json.parse(o.toPrettyJson).transform(addsType("ctf") andThen removesSimpleId) match {
        case JsSuccess(r, _) => r
      }
  }
  implicit val writesSCD: Writes[SimpleCompletedDuel] = new Writes[SimpleCompletedDuel] {
    override def writes(o: SimpleCompletedDuel): JsValue =
      Json.parse(o.toPrettyJson).transform(addsType("duel") andThen removesSimpleId) match {
        case JsSuccess(r, _) => r
      }
  }
  implicit val writesServers: Writes[Servers] = new Writes[Servers] {
    override def writes(o: Servers): JsValue =
      JsObject(o.servers.mapValues(_.getAddress).mapValues(JsString.apply))
  }

  implicit val httpWritesServers = implicitly[Writeable[JsValue]].map(Json.toJson(_: Servers))

}
