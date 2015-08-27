package gg.duel.uservice.clan

import java.time.ZonedDateTime

/**
* Created on 27/08/2015.
*/
case class Clan(id: String, currentPatterns: CurrentPatterns, previousPatterns: List[PreviousPatterns]) {
  def withPatterns(newPatterns: List[String], currentTime: ZonedDateTime): Clan =
    if (currentPatterns.patterns.toSet == newPatterns.toSet) this
    else copy(
      currentPatterns = CurrentPatterns(
        patterns = newPatterns,
        from = currentTime
      ),
      previousPatterns = previousPatterns :+ currentPatterns.toPrevious(currentTime = currentTime)
    )
  def isDerivedFrom(registerClan: RegisterClan): Boolean = {
    id == registerClan.id && currentPatterns.patterns.toSet == registerClan.patterns.toSet
  }
}
object Clan {
  def example = Clan(
    id = "woop",
    currentPatterns = CurrentPatterns(
      patterns = List("w00p|*"),
      from = ZonedDateTime.parse("2006-10-21T00:00:00Z")
    ),
    previousPatterns = List(PreviousPatterns(
      patterns = List("n00b|*"),
      from = ZonedDateTime.parse("2006-10-08T00:00:00Z"),
      to = ZonedDateTime.parse("2006-10-21T00:00:00Z")
    ))
  )
}
