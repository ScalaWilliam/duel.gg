package models

/**
 * Created on 14/08/2015.
 */

case class UserId(userId: String)
object UserId {
  val matchingRegex = """[a-z]{3,10}""".r
  def fromString(string: String): Option[UserId] = {
    PartialFunction.condOpt(string) {
      case matchingRegex() => UserId(string)
    }
  }
}
