package modules

object NicknameMatcher {
  def apply(format: String)(nickname: String): Boolean = {
    if ( format.endsWith("*") && format.startsWith("*") ) {
      nickname.contains(format.drop(1).dropRight(1))
    } else if ( format.endsWith("*") ) {
      nickname.startsWith(format.dropRight(1))
    } else if ( format.startsWith("*") ) {
      nickname.endsWith(format.drop(1))
    } else {
      false
    }
  }
}
