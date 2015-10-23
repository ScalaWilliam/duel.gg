package gg.duel.uservice

/**
 * Created on 27/08/2015.
 */
package object clan {
  def nicknamePatternMatch(pattern: String): Option[String => Boolean] = {
    val startsWith = s"""(.*)\\*""".r
    val endsWith = s"""\\*(.*)""".r
    val contained = s"""\\*(.*)\\*""".r
    PartialFunction.condOpt(pattern) {
      case startsWith(stuff) => (_: String).startsWith(stuff)
      case endsWith(stuff) => (_: String).endsWith(stuff)
      case contained(stuff) => (_: String).contains(stuff)
    }
  }
}
