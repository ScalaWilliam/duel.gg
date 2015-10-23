package gg.duel.uservice.clan

import java.time.ZonedDateTime

/**
 * Created on 27/08/2015.
 */
case class CurrentPatterns(patterns: List[String], from: ZonedDateTime) {
  def multipleMatch: List[String => Boolean] = patterns.flatMap(nicknamePatternMatch)
  def matchNickname(nickname: String): Boolean = {
    multipleMatch.exists(f => f(nickname))
  }
  def toPrevious(currentTime: ZonedDateTime): PreviousPatterns =
    PreviousPatterns(
      patterns = patterns,
      from = from,
      to = currentTime
    )

  def checkInvalidity: Option[String] = {
    if (patterns.isEmpty) Option("No patterns specified - invalid input")
    else if (patterns.forall(str => nicknamePatternMatch(str).isDefined)) Option.empty
    else Option(s"Pattern matches invalid - must be in format *stuff*, *stuff or stuff*")
  }
}
