import org.scalatest.{FunSuite, Matchers}
import scalaz.stream.Process
import us.woop.pinger.Collector
import us.woop.pinger.Collector.GameData
import us.woop.pinger.data.ParsedProcessor.{ParsedTypedMessage, ParsedMessage}
import us.woop.pinger.data.PingPongProcessor.Server
import us.woop.pinger.SauerbratenServerData.Conversions.{Gamemode, ConvertedServerInfoReply}

class CollectorTest extends FunSuite with Matchers {
  import Collector.getGame
  import Collector.GameData
  implicit class whenTran(x: Stream[ParsedMessage]) {
    def afterProcessing: Seq[GameData] = {
      (Process(x:_*) |> getGame).flush
    }
  }
  val payloadA = ConvertedServerInfoReply(1,2,Option(Gamemode(3)),remain=4,5,gamepaused = true,1,"hey","ded")
  val payloadB = ConvertedServerInfoReply(1,2,Option(Gamemode(3)),remain=5,5,gamepaused = true,1,"hey","ded")
  val payloadC = ConvertedServerInfoReply(1,2,Option(Gamemode(3)),remain=6,5,gamepaused = true,1,"hey","ded")
  val infoA = ParsedMessage(Server("123",123),2,payloadA)
  val infoB = ParsedMessage(Server("123",123),2,payloadB)
  val infoC = ParsedMessage(Server("123",123),2,payloadC)
  val infoATyped = ParsedTypedMessage(Server("123",123),2,payloadA)
  val infoBTyped = ParsedTypedMessage(Server("123",123),2,payloadB)
  val infoCTyped = ParsedTypedMessage(Server("123",123),2,payloadC)
  implicit def parsedString(x: String): ParsedMessage =
    ParsedMessage(Server("123", 123),2, x)
  test("Processing one valid item") {
    Stream[ParsedMessage](infoA).afterProcessing should contain only GameData(infoATyped, None, List(infoA))
  }
  test("Processing no items") {
    Stream.empty[ParsedMessage].afterProcessing shouldBe empty
  }
  test("Processing no valid items") {
    Stream[ParsedMessage]("Hey").afterProcessing shouldBe empty
  }
  test("Processing one valid, + another item") {
    Stream[ParsedMessage](infoA, "test").afterProcessing should contain only GameData(infoATyped, None, List(infoA, "test"))
  }
  test("Processing one valid + another two") {
    Stream[ParsedMessage](infoA, "test", "testB").afterProcessing should contain only GameData(infoATyped, None, List(infoA, "test", "testB"))
  }
  test("Unused, + new") {
    Stream[ParsedMessage]("test", infoA, "testB").afterProcessing should contain only GameData(infoATyped, None, List(infoA, "testB"))
  }
  test("Transitioning into another game") {
    Stream(infoA, infoB).afterProcessing should contain only (
      GameData(infoATyped, Option(infoBTyped), List(infoA)),
      GameData(infoBTyped, None, List(infoB))
    )
  }
  test("Transitioning into a third game") {
    Stream(infoA, infoB, infoC).afterProcessing should contain only (
      GameData(infoATyped, Option(infoBTyped), List(infoA)),
      GameData(infoBTyped, Option(infoCTyped), List(infoB)),
      GameData(infoCTyped, None, List(infoC))
    )
  }
  test("Transitioning with pick ups") {
    Stream[ParsedMessage](infoA, "hey", "hez", infoB, "hea", "heb").afterProcessing should contain only (
      GameData(infoATyped, Option(infoBTyped), List(infoA, "hey", "hez")),
      GameData(infoBTyped, None, List(infoB, "hea", "heb"))
    )
  }
  test("Transitioning with pick ups with junk at start") {
    Stream[ParsedMessage]("Bang", "Doog", infoA, "hey", "hez", infoB, "hea", "heb").afterProcessing should contain only (
      GameData(infoATyped, Option(infoBTyped), List(infoA, "hey", "hez")),
      GameData(infoBTyped, None, List(infoB, "hea", "heb"))
    )
  }
}