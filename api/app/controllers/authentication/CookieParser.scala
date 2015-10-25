package controllers.authentication

import play.api.mvc.Cookie

case class CookieParser(salt: String) {
  val signedParser = SignedParser(salt = salt)
  def unapply(cookie: Cookie): Option[AuthenticationUser] = {
    signedParser.fromSignedString.unapply(cookie.value)
  }
}
