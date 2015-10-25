package controllers.authentication

import java.time.LocalDateTime
import java.util.Base64

import org.apache.commons.codec.binary.Hex
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Cookie


case class AuthenticationUser(google: String, countryCode: Option[String], id: Option[String], expires: LocalDateTime) {
  def isExpired = expires.isAfter(LocalDateTime.now())
  def expiresSoon(days: Int) = expires.isAfter(LocalDateTime.now().plusDays(days))
  def newExpiry(defaultExpiryDays: Int) = copy(expires = LocalDateTime.now().plusDays(defaultExpiryDays))
  def toJson = Json.toJson(this)(AuthenticationUser.auFormats)

  def toJsonAndCompact(salt: String) = JsObject {
    Map(
      "authentication" -> toJson,
      "signed" -> JsString(cookieValue("salt"))
    )
  }

  def cookieValue(salt: String) = {
    val json = toJson.toString()
    val encodedJson = Hex.encodeHexString(Base64.getEncoder.encode(json.getBytes("UTF-8")))
    val hashed = hash(s"$salt:$json")
    s"$encodedJson:$hashed"
  }
  def asCookie(cookieName: String, salt: String) = Cookie(name = cookieName, value = cookieValue(salt))
}

object AuthenticationUser {
  implicit val auFormats = Json.format[AuthenticationUser]
  object fromString {
    def unapply(jsonString: String): Option[AuthenticationUser] = {
      Json.fromJson[AuthenticationUser](Json.parse(jsonString)).asOpt
    }
  }
}
