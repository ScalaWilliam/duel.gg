package gg.duel.pinger.analytics.duel

import scala.xml.UnprefixedAttribute



object SimpleCompletedDuel {
  def fromPrettyJson(json: String): SimpleCompletedDuel = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, writePretty}
    implicit val formats = Serialization.formats(NoTypeHints)
    read[SimpleCompletedDuel](json)
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
  serverDescription: String,
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

}