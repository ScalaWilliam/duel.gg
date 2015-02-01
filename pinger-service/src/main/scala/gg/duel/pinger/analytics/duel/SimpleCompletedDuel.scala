package gg.duel.pinger.analytics.duel

import scala.xml.UnprefixedAttribute

case class LiveDuel
(
  simpleId: String,
  duration: Int,
  playedAt: List[Int],
  startTimeText: String,
  startTime: Long,
  secondsRemaining: Int,
  map: String,
  mode: String,
  server: String,
  players: Map[String, SimplePlayerStatistics],
  winner: Option[String], metaId: Option[String]
  ) {
  def asScd = SimpleCompletedDuel(simpleId, duration, playedAt, startTimeText, startTime, map, mode, server, players, winner, metaId)
  def toJson = asScd.toJson
  def toPrettyJson = asScd.toPrettyJson
  def toXml = {
    val xml = asScd.toXml.copy(label = "live-duel")
    xml.copy(attributes = new UnprefixedAttribute("seconds-remaining", secondsRemaining.toString, xml.attributes))
  }
}

case class SimpleCompletedDuel
(
  simpleId: String,
  duration: Int,
  playedAt: List[Int],
  startTimeText: String,
  startTime: Long,
  map: String,
  mode: String,
  server: String,
  players: Map[String, SimplePlayerStatistics],
  winner: Option[String], metaId: Option[String]) {
  def toJson = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, write}
    implicit val formats = Serialization.formats(NoTypeHints)
    write(this)
  }
  def toPrettyJson = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, writePretty}
    implicit val formats = Serialization.formats(NoTypeHints)
    writePretty(this)
  }
  def toXml = <completed-duel
  simple-id={simpleId}
  meta-id={metaId.orNull}
  duration={s"$duration"}
  start-time={startTimeText}
  map={map}
  mode={mode}
  server={server}
  winner={winner.orNull}>
    <played-at>{playedAt.mkString(" ")}</played-at>
    {for {(name, stats) <- players} yield
    <player name={name} partial-ip={stats.ip} frags={s"${stats.frags}"} accuracy={s"${stats.accuracy}"}
            weapon={stats.weapon}>
      {for {(at, frags) <- stats.fragLog}
    yield
      <frags at={at.toString}>{frags}</frags>}
    </player>
    }
  </completed-duel>


}