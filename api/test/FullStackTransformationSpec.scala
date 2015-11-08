import gg.duel.pinger.analytics.duel.StubGenerator
import gg.duel.transformer.GameNode
import gg.duel.transformer.lookup.BasicLookingUp
import lib.GeoLookup
import modules.JsonGameToSimpleGame
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class FullStackTransformationSpec extends WordSpec with Matchers with OptionValues {

  val fD = StubGenerator.validSequenceCompletedDuel
  val testEnricher = new BasicLookingUp(
    demoLookup = _ => Option("http://test.dmo"),
    clanLookup = name => if ( name.startsWith("w00p|") ) Option("woop") else Option.empty,
    countryLookup = GeoLookup.apply
  )
  val gameNode = GameNode(
    jsonString = fD.toJson,
    plainGameEnricher = testEnricher
  )

  "Full stack" must {
    "do it" in {
      gameNode.enrich()
      val enrichedGamePretty = gameNode.asPrettyJson
      val expectedGamePretty = Json.prettyPrint(Json.parse(getClass.getResourceAsStream("test-output.json")))
      enrichedGamePretty shouldBe expectedGamePretty
    }
    "ensure it is idempotent" in {
      val enrichedGame = gameNode.asPrettyJson
      gameNode.enrich()
      val secondlyEnrichedGame = gameNode.asPrettyJson
      enrichedGame shouldBe secondlyEnrichedGame
    }
    "extract correctly" in {
      val k = JsonGameToSimpleGame(
        enricher = testEnricher
      ).apply(json = fD.toJson).value
      k.clans should contain only ("woop")
      k.demo.value shouldBe "http://test.dmo"
      k.gameType shouldBe "duel"
      k.id shouldBe "1970-01-01T03:25:12Z"
      k.map shouldBe "academy"
      k.players should contain only ("w00p|Art", "w00p|Drakas")
      k.server shouldBe "123.2.2.22:2134"
      k.tags should contain only ("duel")
      k.users shouldBe empty
    }
  }
}