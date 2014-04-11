import org.scalatest.{Matchers, FlatSpec}
import pinger.GameMaker
import us.woop.pinger.Collector.GameData
import us.woop.pinger.data.ParsedProcessor.{ParsedMessage, ParsedTypedMessage}
import us.woop.pinger.data.PingPongProcessor
import us.woop.pinger.SauerbratenServerData.Conversions.{Gamemode, ConvertedServerInfoReply}
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo

class GameMakerTest extends FlatSpec with Matchers {

  val server: PingPongProcessor.Server = PingPongProcessor.Server("123.3.3.3",12312)

  val time: Long = 12321312

  "meh" should "do this" in {
    val sample = PlayerExtInfo(1, 1, 1, "John", "", 2, 2, 2, 30, 13, 2, 4, 4, 5, "1232") ::
      PlayerExtInfo(1, 1, 1, "Don", "", 2, 2, 2, 30, 13, 2, 4, 4, 5, "1232") :: Nil
    val gameData = GameData(
      firstTime = ParsedTypedMessage(server, time, ConvertedServerInfoReply(2,2,Option(Gamemode(1)),5,3,gamepaused = false,4,"hades", "bang")),
      nextGame = None,
      data = for {
        (data, idx) <- List.fill(500)(sample).flatten.zipWithIndex
        m = ParsedMessage(server, time + idx * 2000, data)
      } yield m
    )
    val output = GameMaker.process(gameData)
    println(output)
    fail("wut")
  }
}
