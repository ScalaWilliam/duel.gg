package gg.duel.uservice.clan

import java.time.ZonedDateTime

/**
* Created on 27/08/2015.
*/
case class RegisterClan(id: String, patterns: List[String], startTime: Option[ZonedDateTime]) {
  def toClan(currentTime: ZonedDateTime) =
    Clan(
      id = id,
      currentPatterns = CurrentPatterns(
        patterns = patterns,
        from = startTime.getOrElse(currentTime)
      ),
      previousPatterns = List.empty
    )
}
