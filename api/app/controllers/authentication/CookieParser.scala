package controllers.authentication

import java.util.Base64

import org.apache.commons.codec.binary.Hex
import play.api.mvc.Cookie

import scala.util.Try


case class CookieParser(salt: String) {

  def unapply(cookie: Cookie): Option[AuthenticationUser] = {
    PartialFunction.condOpt(cookie.value.split(":").toList) {
      case decoded(json@AuthenticationUser.fromString(authenticationUser)) :: signature :: Nil
        if hash(s"$salt:$json") == signature =>
        authenticationUser
    }
  }

  object decoded {
    def unapply(inputString: String): Option[String] = {
      Try(new String(Base64.getDecoder.decode(Hex.decodeHex(inputString.getBytes("UTF-8").map(_.toChar))))).toOption
    }
  }
}
