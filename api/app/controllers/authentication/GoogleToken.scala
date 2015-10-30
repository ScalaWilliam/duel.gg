package controllers.authentication

import play.api.libs.json.{JsValue, Json}

import scala.util.Try


/**
 * @param aud Master Google ID
 * @param sub ID to access OAuth services with
 * @param exp Expiry of this token as Int
 */
case class GoogleToken(email: String, email_verified: String, aud: String, sub: String, exp: String) {
  def isValid(expectedAud: String) = email_verified == "true" &&
    aud == expectedAud &&
    Try(exp.toLong).toOption.exists(_ >= System.currentTimeMillis() / 1000L)
}
object GoogleToken {
  implicit val gtReads = Json.reads[GoogleToken]
  object fromJson {
    def unapply(jsValue: JsValue): Option[GoogleToken] = {
      gtReads.reads(jsValue).asOpt
    }
  }
}