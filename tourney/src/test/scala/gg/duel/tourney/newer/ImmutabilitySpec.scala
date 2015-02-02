package gg.duel.tourney.newer

import gg.duel.tourney.newer.Immutability._
import org.scalatest.{Matchers, WordSpec}

class ImmutabilitySpec extends WordSpec with Matchers {
  "Basic tournament" must {
    val initial = Tournament.fourPlayers(Vector("A", "B", "C", "D"))
    "Record one winner" in {
      val firstWinner = initial.withGameWinner(1, "A")
      firstWinner.games(1) shouldBe GameWon("A")
      firstWinner.games(2) shouldBe GameAwaitingResult
      firstWinner.games(3) shouldBe GameAwaitingSlots
      println(firstWinner)
      firstWinner.slots(1) shouldBe SlotFilled("A")
      firstWinner.slots(2) shouldBe SlotFilled("B")
      firstWinner.slots(3) shouldBe SlotFilled("C")
      firstWinner.slots(4) shouldBe SlotFilled("D")
      firstWinner.slots(5) shouldBe SlotFilled("A")
      firstWinner.slots(6) shouldBe SlotAwaiting

      firstWinner.resultsWouldCompleteGames shouldBe Set((("C", "D"), 2))
    }
    "Record one winner (other side)" in {
      val firstWinner = initial.withGameWinner(2, "C")
      firstWinner.games(1) shouldBe GameAwaitingResult
      firstWinner.games(2) shouldBe GameWon("C")
      firstWinner.games(3) shouldBe GameAwaitingSlots
      firstWinner.slots(1) shouldBe SlotFilled("A")
      firstWinner.slots(2) shouldBe SlotFilled("B")
      firstWinner.slots(3) shouldBe SlotFilled("C")
      firstWinner.slots(4) shouldBe SlotFilled("D")
      firstWinner.slots(5) shouldBe SlotAwaiting
      firstWinner.slots(6) shouldBe SlotFilled("C")
      firstWinner.resultsWouldCompleteGames shouldBe Set((("A", "B"), 1))
    }
    "Record two winners, move to another game" in {
      val result = initial.withGameWinner(1, "A").withGameWinner(2, "C")
      val differentOrderResult = initial.withGameWinner(2, "C").withGameWinner(1, "A")
      // ensure ops are associative
      result shouldBe differentOrderResult

      result.games(1) shouldBe GameWon("A")
      result.games(2) shouldBe GameWon("C")
      result.games(3) shouldBe GameAwaitingResult
      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFilled("A")
      result.slots(6) shouldBe SlotFilled("C")
      result.resultsWouldCompleteGames shouldBe Set((("A", "C"), 3))
    }
    "Record two winners, accept next winner" in {
      val result = initial.withGameWinner(1, "A").withGameWinner(2, "C").withGameWinner(3, "A")
      result.games(1) shouldBe GameWon("A")
      result.games(2) shouldBe GameWon("C")
      result.games(3) shouldBe GameWon("A")
      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFilled("A")
      result.slots(6) shouldBe SlotFilled("C")

      result.resultsWouldCompleteGames shouldBe empty
    }
    "Record one slot failure with other awaiting" in {
      val result = initial.withSlotFailed(5, "Blah")
      result.games(1) shouldBe GameFailed("Downstream slot 5 failed with reason: Blah")
      result.games(2) shouldBe GameAwaitingResult
      result.games(3) shouldBe GameAwaitingSlots
      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFailed("Blah")
      result.slots(6) shouldBe SlotAwaiting
      result.resultsWouldCompleteGames shouldBe Set((("C", "D"), 2))
    }
    "Try on the other side way round" in {
      val result = initial.withSlotFailed(6, "Blah")
      result.games(1) shouldBe GameAwaitingResult
      result.games(2) shouldBe GameFailed("Downstream slot 6 failed with reason: Blah")
      result.games(3) shouldBe GameAwaitingSlots
      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotAwaiting
      result.slots(6) shouldBe SlotFailed("Blah")
      result.resultsWouldCompleteGames shouldBe Set((("A", "B"), 1))
    }
    "Two failures fail game" in {
      val result = initial.withSlotFailed(5, "First").withSlotFailed(6, "Second")
      val oppositeResult = initial.withSlotFailed(6, "Second").withSlotFailed(5, "First")
      result shouldBe oppositeResult
      result.games(1) shouldBe GameFailed("Downstream slot 5 failed with reason: First")
      result.games(2) shouldBe GameFailed("Downstream slot 6 failed with reason: Second")

      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFailed("First")
      result.slots(6) shouldBe SlotFailed("Second")
      result.games(3) shouldBe GameFailed("Both slots 5 and 6 failed: (First, Second)")
      result.resultsWouldCompleteGames shouldBe empty
    }
    "One fail, one win gives an auto-win" in {
      val result = initial.withSlotFailed(5, "First").withGameWinner(2, "C")
      val oppositeResult = initial.withGameWinner(2, "C").withSlotFailed(5, "First")
      result shouldBe oppositeResult
      result.games(1) shouldBe GameFailed("Downstream slot 5 failed with reason: First")
      result.games(2) shouldBe GameWon("C")
      result.games(3) shouldBe GameWon("C")

      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFailed("First")
      result.slots(6) shouldBe SlotFilled("C")
      result.resultsWouldCompleteGames shouldBe empty
    }
    "Record two winners, followed by another winner" in {
      val result = initial.withGameWinner(1, "A").withGameWinner(2, "C").withGameWinner(3, "A")
      result.games(1) shouldBe GameWon("A")
      result.games(2) shouldBe GameWon("C")
      result.games(3) shouldBe GameWon("A")

      result.slots(1) shouldBe SlotFilled("A")
      result.slots(2) shouldBe SlotFilled("B")
      result.slots(3) shouldBe SlotFilled("C")
      result.slots(4) shouldBe SlotFilled("D")
      result.slots(5) shouldBe SlotFilled("A")
      result.slots(6) shouldBe SlotFilled("C")

      result.resultsWouldCompleteGames shouldBe empty
    }
  }
}