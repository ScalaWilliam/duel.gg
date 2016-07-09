package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.duel.{CompletedDuel, SimpleCompletedDuel}
import org.scalatest.{Matchers, WordSpec}

class JsonSerDeserSpec extends WordSpec with Matchers {
  "Simple Duel" must {
    "Serialize and deserialize" in {
      val scd = CompletedDuel.test.toSimpleCompletedDuel
      SimpleCompletedDuel.fromPrettyJson(scd.toPrettyJson) shouldBe scd
    }
  }
}
