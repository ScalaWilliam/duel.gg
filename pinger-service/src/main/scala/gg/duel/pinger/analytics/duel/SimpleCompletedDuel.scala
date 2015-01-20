package gg.duel.pinger.analytics.duel

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
    <players>
      {for {(name, stats) <- players} yield
      <player name={name} ip={stats.ip} frags={s"${stats.frags}"} accuracy={s"${stats.accuracy}"}
              weapon={stats.weapon}>
        {for {(at, frags) <- stats.fragLog}
      yield
        <frags at={at.toString}>{frags}</frags>}
      </player>
      }
    </players>
  </completed-duel>


}