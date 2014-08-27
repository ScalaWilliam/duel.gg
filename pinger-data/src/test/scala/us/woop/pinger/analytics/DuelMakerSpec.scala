package us.woop.pinger.analytics

import org.scalatest.{Matchers, WordSpec}
import us.woop.pinger.analytics.StreamedDuelMaker.{ZFoundGame, ZRejectedDuelState}
import us.woop.pinger.analytics.stub.StubGenerator

class DuelMakerSpec extends WordSpec with Matchers {

  import us.woop.pinger.analytics.stub.StubGenerator._

  "Duel maker" must {

    /** Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName}")) */
    "Fail when first message is not a CSIR" in {
      val result = states(
        pei("lol", 1, "123")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Input not a Converted")
    }

    /** Bad(One(s"Expected 2 or more clients, got ${message.clients}")) */
    "Fail when less than 2 clients" in {
      val result = states(
        csr(0, 3, 10, "academy")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Expected 2 or more clients")
    }

    /** Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)")) */
    "Fail when required time less than 550 seconds" in {
      val result = states(
        csr(2, 3, 120, "academy")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Time remaining not enough")
    }

    /** Bad(One(s"Mode $other (${message.gamemode}) not in $duelModeNames")) */
    "Fail when not a duel mode" in {
      val result = states(
        csr(2, 2, 600, "academy")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("not a duel mode")
    }

    /** Bad(One(s"Game has finished unexpectedly as now ${update.clients} clients")) */
    "Fail when number of clients goes below 2" in {
      val result = states(
        csr(2, 2, 600, "academy"),
        csr(1, 2, 500, "academy")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Expected 2 or more clients")
    }

    /** Bad(One(s"Game was active ${gameActive.size} minutes, expected at least 8 minutes")) */
    "Fail if not game was active for less than 8 minutes" in {
      val result = timedStates(
        0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
        1 * 60 -> csr(2, 3, 600 - (1 * 60), "academy"),
        2 * 60 -> csr(2, 3, 600 - (2 * 60), "academy"),
        3 * 60 -> csr(2, 3, 600 - (3 * 60), "academy"),
        4 * 60 -> csr(2, 3, 600 - (4 * 60), "academy"),
        5 * 60 -> csr(2, 3, 600 - (5 * 60), "academy"),
        6 * 60 -> csr(2, 3, 600 - (6 * 60), "academy"),
        7 * 60 -> csr(2, 3, 600 - (7 * 60), "academy"),
        8 * 60 -> csr(2, 2, 500, "tartech")
      )
      result.last shouldBe a [ZRejectedDuelState]

      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("expected at least 8 minutes")
    }

    "Fail for other reasons if game was over 8 minutes" in {
      val result = timedStates(
        0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
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
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should not include "expected at least 8 minutes"


      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Player stats were unavailable at the end of the game")
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Game had != 2 players")
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Game does not have unique player names")
    }

    /** Bad(One(s"Player had an empty frag log")) */

    /** Bad(One(s"Player stats were unavailable at the end of the game: have $latestPlayerStatistic")) */

    "Fail if not enough player stats provider at a game switch" in {
      val result = timedStates(
        0 -> csr(2, 3, 600, "academy"),
        100 -> csr(2, 2, 500, "tartech")
      )
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Player stats were unavailable at the end of the")
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Game had != 2 players")
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include("Game does not have unique player names")
    }


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
      result.last shouldBe a [ZRejectedDuelState]
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Player stats were unavailable at the end of the game")
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Game had != 2 players")
      // as no player at all
      result.last.asInstanceOf[ZRejectedDuelState].cause.toString should include ("Game does not have unique player names")
    }

    /** Bad(One(s"Game had != 2 players, found $playerStatistics")) */
    /** Bad(One(s"Game does not have unique player names. Found: $uniqueNames")) */
    // redundant - if we find any regressions in prod then we'll add tests. Seems that we've got the above in the previous tests anyway.


    /** Now for the positive tests - when is a game Ok? **/
    "Succeed to get a nice game out of it" in {

      val result = timedStates(StubGenerator.validSequence :_*)

      result.last shouldBe a [ZFoundGame]
      val completedDuel = result.last.asInstanceOf[ZFoundGame].completedDuel

      completedDuel.duration shouldBe 9
      val simpleDuel = completedDuel.toSimpleCompletedDuel
      simpleDuel.map shouldBe "academy"
      simpleDuel.mode shouldBe "instagib"
      simpleDuel.duration shouldBe 9
      simpleDuel.playedAt should contain only (2, 3, 4, 5, 6, 7, 8, 9)
      simpleDuel.players should have size 2
      simpleDuel.players.keySet should contain only ("w00p|Drakas", "w00p|Art")
      simpleDuel.winner shouldBe Option("w00p|Drakas")
      simpleDuel.server shouldBe "123.2.2.22:2134"

      val drakas = simpleDuel.players("w00p|Drakas")
      drakas.ip shouldBe "123"
      drakas.frags shouldBe 4
      drakas.name shouldBe "w00p|Drakas"
      drakas.fragLog should contain only ("2" -> 2, "9" -> 4)
      drakas.weapon shouldBe "shotgun"

      val art = simpleDuel.players("w00p|Art")
      art.frags shouldBe 3
      art.name shouldBe "w00p|Art"
      art.ip shouldBe "123"
      art.fragLog should contain only ("2" -> 2, "9" -> 3)
      art.weapon shouldBe "shotgun"
    }
  }

}
