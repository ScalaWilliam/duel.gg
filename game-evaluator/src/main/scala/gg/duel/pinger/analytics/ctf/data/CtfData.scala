package gg.duel.pinger.analytics.ctf.data

import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

case class PlayerId(name: String, ip: String, team: String)

case class SimpleTeamStatistics(name: String, flags: Int, players: Map[PlayerId, Weapon])

case class Team(name: String)

case class SimpleCTF(duration: Int, playedAt: List[Int], teams: Map[Team, SimpleTeamStatistics])

case class SimplePlayer(name: String, ip: String, weapon: String)

case class SimpleTeamScore(name: String, flags: Int, flagLog: List[(Int, Int)], players: List[SimplePlayer])

case class SimpleCompletedCTF
(simpleId: String, teamsize: Int, duration: Int, playedAt: List[Int],
 startTimeText: String, startTime: Long, map: String, mode: String,
 server: String, teams: Map[String, SimpleTeamScore],
 winner: Option[String], metaId: Option[String]) {
  def toPrettyJson = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, writePretty}
    implicit val formats = Serialization.formats(NoTypeHints)
    writePretty(this)
  }
  def toJson = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, write}
    implicit val formats = Serialization.formats(NoTypeHints)
    write(this)
  }
}

object SimpleCompletedCTF {

  def fromPrettyJson(json: String): SimpleCompletedCTF = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, writePretty}
    implicit val formats = Serialization.formats(NoTypeHints)
    read[SimpleCompletedCTF](json)
  }
  def test = {
    val t = System.currentTimeMillis
    SimpleCompletedCTF(
      teamsize = 2,
      simpleId = "yay",
      duration = 5,
      playedAt = List(1, 2, 3, 4, 5),
      startTimeText = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(t),
      startTime = t,
      map = "reissen",
      mode = "efficiency ctf",
      server = "localhost:123",
      winner = Option("evil"),
      teams = Map(
        "evil" -> SimpleTeamScore(name = "evil", 5, flagLog = List(1 -> 1, 2 -> 2, 3 -> 4, 4 -> 4, 5 -> 5), players = List(SimplePlayer(name = "Drakas", ip = "85.214.61.x", weapon = "rifle"))),
        "good" -> SimpleTeamScore(name = "good", 2, flagLog = List(1 -> 1, 2 -> 2, 3 -> 2, 4 -> 2, 5 -> 2), players = List(SimplePlayer(name = "Art", ip = "87.98.216.x", weapon = "rifle")))
      ),
      metaId = None
    )
  }
}

case class SecondsRemaining(seconds: Int)

case class Flags(flags: Int)

case class Weapon(weapon: String)

case class Accuracy(accuracy: Int)

case class TeamId(name: String)

case class TeamLogItem(teamId: TeamId, remaining: SecondsRemaining, flags: Flags)

case class PlayerLogItem(playerId: PlayerId, remaining: SecondsRemaining, weapon: Weapon)