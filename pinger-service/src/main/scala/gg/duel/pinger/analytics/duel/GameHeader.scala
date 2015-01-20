package gg.duel.pinger.analytics.duel
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

case class GameHeader(startTime: Long, startMessage: ConvertedServerInfoReply, server: String, mode: String, map: String) {
  def startTimeText: String = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(startTime)
}
object GameHeader {
  val testServer = "test:1234"
  def test = GameHeader(
    startTime = System.currentTimeMillis,
    startMessage = ConvertedServerInfoReply(0,0,0,0,0,true,0,"test","test"),
    server = testServer, mode = "test", map = "test"
  )
}
