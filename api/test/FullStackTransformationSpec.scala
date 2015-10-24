import gcc.enrichment.{Enricher, PlayerLookup}
import gg.duel.pinger.analytics.duel.StubGenerator
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class FullStackTransformationSpec extends WordSpec with Matchers {

  val fD = StubGenerator.validSequenceCompletedDuel
  val enricher = new Enricher(new PlayerLookup {
    override def lookupUserId(s: String, dateTime: DateTime): String = if (s == "w00p|Drakas") "drakas" else null

    override def lookupClanId(s: String, dateTime: DateTime): String = if (s.startsWith("w00p|")) "woop" else null
  })

  "full stack" must {
    "do it" in {

      val enrichedGame = enricher.enrichJsonGame(fD.toJson)

      val enrichedGamePretty = Json.prettyPrint(Json.parse(enrichedGame))
      val expectedGamePretty = Json.prettyPrint(Json.parse(getClass.getResourceAsStream("test-output.json")))

      enrichedGamePretty shouldBe expectedGamePretty
    }
    "ensure it is idempotent" in {
      val enrichedGame = enricher.enrichJsonGame(fD.toJson)
      val secondlyEnrichedGame = enricher.enrichJsonGame(enrichedGame)
      enrichedGame shouldBe secondlyEnrichedGame
    }
  }
}