import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import models._
import play.api.libs.json.{Json, JsString, JsValue, Writes}

/**
 * Created on 14/08/2015.
 */
package object controllers {
  implicit val zdt: Writes[ZonedDateTime] = new Writes[ZonedDateTime] {
    override def writes(o: ZonedDateTime): JsValue = JsString(o.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
  }
  implicit val currentNickname = Json.writes[CurrentNickname]
  implicit val setNickname = Json.format[SetNickname]
  implicit val registerUserFmt = Json.format[RegisterUser]
  implicit val previousNickname = Json.writes[PreviousNickname]
  implicit val writeUser = Json.writes[FullUser]

}
