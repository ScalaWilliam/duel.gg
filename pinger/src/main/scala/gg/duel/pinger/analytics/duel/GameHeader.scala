package gg.duel.pinger.analytics.duel
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

case class GameHeader(startTime: Long, startMessage: ConvertedServerInfoReply, mode: String, map: String) {
  def startTimeText: String = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(startTime)
}
object GameHeader {
  def test = GameHeader(
    startTime = System.currentTimeMillis,
    startMessage = ConvertedServerInfoReply(
      clients = 0,
      protocol = 0,
      gamemode = 0,
      remain = 0,
      maxclients = 0,
      gamepaused = true,
      gamespeed = 0,
      mapname = "test",
      description = "test"
    ),
    mode = "test", map = "test"
  )
}
