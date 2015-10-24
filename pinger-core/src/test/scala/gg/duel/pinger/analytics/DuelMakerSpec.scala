package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZRejectedGameState, ZIteratorState, ZFoundDuel, ZRejectedDuelState}
import gg.duel.pinger.analytics.duel.StubGenerator
import org.scalatest.{Matchers, WordSpec}

class DuelMakerSpec extends WordSpec with Matchers {

  import StubGenerator._

  implicit class failedAdder(input: List[ZIteratorState]) {
    def shouldFailWith[T](f: ZRejectedGameState => T) = {
      input.last shouldBe a [ZRejectedGameState]
      f(input.last.asInstanceOf[ZRejectedGameState])
    }
    def shouldFailWithDuel[T](f: ZRejectedDuelState => T) = {
      input.last shouldBe a [ZRejectedDuelState]
      f(input.last.asInstanceOf[ZRejectedDuelState])
    }
  }

  "Duel maker" must {

    /** Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName}")) */
    "Fail when first message is not a CSIR" in {
      states(
        pei("lol", 1, "123")
      ) shouldFailWith {
        _.duelCause.toString should include("Input not a Converted")
      }
    }

    /** Bad(One(s"Expected 2 or more clients, got ${message.clients}")) */
    "Fail when less than 2 clients" in {
      states(
        csr(0, 3, 10, "academy")
      ) shouldFailWith {
        _.duelCause.toString should include("Expected 2 or more clients")
      }
    }

    /** Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)")) */
    "Fail when required time less than 550 seconds" in {
      states(
        csr(2, 3, 120, "academy")
      ) shouldFailWith {
        _.duelCause.toString should include("Time remaining not enough")
      }
    }

    /** Bad(One(s"Mode $other (${message.gamemode}) not in $duelModeNames")) */
    "Fail when not a duel mode" in {
      states(
        csr(2, 2, 600, "academy")
      ) shouldFailWith {
        _.duelCause.toString should include("not a duel mode")
      }
    }

    /** Bad(One(s"Game has finished unexpectedly as now ${update.clients} clients")) */
    "Fail when number of clients goes below 2" in {
      states(
        csr(2, 2, 600, "academy"),
        csr(1, 2, 500, "academy")
      ) shouldFailWith {
        _.duelCause.toString should include("Expected 2 or more clients")
      }
    }

    /** Bad(One(s"Game was active ${gameActive.size} minutes, expected at least 8 minutes")) */
    "Fail if not game was active for less than 8 minutes" in {
      val result = timedStates(
        0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
        0 * 60 -> pei("w00p|Drakas", 4, "123"),
        0 * 60 -> pei("w00p|Art", 3, "123"),
        1 * 60 -> csr(2, 3, 600 - (1 * 60), "academy"),
        2 * 60 -> csr(2, 3, 600 - (2 * 60), "academy"),
        3 * 60 -> csr(2, 3, 600 - (3 * 60), "academy"),
        4 * 60 -> csr(2, 3, 600 - (4 * 60), "academy"),
        5 * 60 -> csr(2, 3, 600 - (5 * 60), "academy"),
        6 * 60 -> csr(2, 3, 600 - (6 * 60), "academy"),
        7 * 60 -> pei("w00p|Drakas", 4, "123"),
        7 * 60 -> pei("w00p|Art", 3, "123"),
        7 * 60 -> csr(2, 3, 600 - (7 * 60), "academy"),
        8 * 60 -> csr(2, 2, 500, "tartech")
      )
      result shouldFailWithDuel {_.cause.toString should include("Expected at least 8 minutes") }
    }

    "Fail for other reasons if game was over 8 minutes" in {
      val result = timedStates(
        0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
        0 * 60 -> pei("w00p|Drakas", 4, "123"),
        0 * 60 -> pei("w00p|Art", 3, "123"),
        1 * 60 -> csr(2, 3, 600 - (1 * 60), "academy"),
        2 * 60 -> csr(2, 3, 600 - (2 * 60), "academy"),
        3 * 60 -> csr(2, 3, 600 - (3 * 60), "academy"),
        4 * 60 -> csr(2, 3, 600 - (4 * 60), "academy"),
        5 * 60 -> csr(2, 3, 600 - (5 * 60), "academy"),
        6 * 60 -> csr(2, 3, 600 - (6 * 60), "academy"),
        7 * 60 -> csr(2, 3, 600 - (7 * 60), "academy"),
        7 * 60 -> pei("w00p|Art", 3, "123"),
        8 * 60 -> csr(2, 3, 600 - (8 * 60), "academy"),
        8 * 60 -> pei("w00p|Drakas", 4, "123"),
        4 * 60 -> csr(2, 2, 500, "tartech")
      )
      result shouldFailWithDuel { r =>
        val cause = r.cause.toString
        cause should not include "expected at least 8 minutes"
        cause should include("Could not find a log item to say that both players finished the game")
      }
    }

    /** Bad(One(s"Player had an empty frag log")) */

    /** Bad(One(s"Player stats were unavailable at the end of the game: have $latestPlayerStatistic")) */


    "Fail to display player stats when he is a spectator" in {
      val result = timedStates(
        0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
        // state above 3, meaning spectator, for example
        1 * 60 -> pei("lol", 2, "123").copy(state = 4),
        1 * 60 -> csr(2, 3, 600 - (1 * 60), "academy"),
        2 * 60 -> csr(2, 3, 600 - (2 * 60), "academy"),
        3 * 60 -> csr(2, 3, 600 - (3 * 60), "academy"),
        4 * 60 -> csr(2, 3, 600 - (4 * 60), "academy"),
        5 * 60 -> csr(2, 3, 600 - (5 * 60), "academy"),
        6 * 60 -> csr(2, 3, 600 - (6 * 60), "academy"),
        7 * 60 -> csr(2, 3, 600 - (7 * 60), "academy"),
        8 * 60 -> csr(2, 3, 600 - (8 * 60), "academy"),
        4 * 60 -> csr(2, 2, 500, "tartech")
      )
      result shouldFailWithDuel { r =>
        val cause = r.cause.toString
        cause should include("Expected exactly two players")
      }
    }

    /** Bad(One(s"Game had != 2 players, found $playerStatistics")) */
    /** Bad(One(s"Game does not have unique player names. Found: $uniqueNames")) */
    // redundant - if we find any regressions in prod then we'll add tests. Seems that we've got the above in the previous tests anyway.

    /** Now for the positive tests - when is a game Ok? **/
    "Succeed to get a nice game out of it" in {

      val result = timedStates(StubGenerator.validSequence :_*)

      result.last shouldBe a [ZFoundDuel]
      val completedDuel = result.last.asInstanceOf[ZFoundDuel].completedDuel

      completedDuel.duration shouldBe 10
      val simpleDuel = completedDuel
      simpleDuel.map shouldBe "academy"
      simpleDuel.mode shouldBe "instagib"
      simpleDuel.duration shouldBe 10
      simpleDuel.playedAt should contain only (1, 11)
      simpleDuel.players should have size 2
      simpleDuel.players.keySet should contain only ("w00p|Drakas", "w00p|Art")
      simpleDuel.winner shouldBe Option("w00p|Drakas")
      simpleDuel.server shouldBe "123.2.2.22:2134"

      val drakas = simpleDuel.players("w00p|Drakas")
      drakas.ip shouldBe StubGenerator.drakasUkIp
      drakas.frags shouldBe 40
      drakas.accuracy shouldBe 25
      drakas.name shouldBe "w00p|Drakas"
      drakas.fragLog should contain only (2 -> 2, 10 -> 40)
      drakas.weapon shouldBe "shotgun"

      val art = simpleDuel.players("w00p|Art")
      art.frags shouldBe 30
      art.accuracy shouldBe 25
      art.name shouldBe "w00p|Art"
      art.ip shouldBe StubGenerator.artBosniaIp
      art.fragLog should contain only (2 -> 2, 10 -> 30)
      art.weapon shouldBe "shotgun"
    }
  }

}
