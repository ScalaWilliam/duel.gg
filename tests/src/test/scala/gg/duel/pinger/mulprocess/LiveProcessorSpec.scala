package gg.duel.pinger.mulprocess

import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.{ZOutOfGameState, ZIteratorState}
import gg.duel.pinger.analytics.duel.StubGenerator
import gg.duel.pinger.data.Server
import org.scalatest.{Inside, Matchers, OptionValues, WordSpec}

class LiveProcessorSpec
  extends WordSpec
  with OptionValues
  with Matchers
  with Inside
{
  val server = Server.stub
  "it" must {
    "work as expected" in {
      val ts: List[ZIteratorState] = StubGenerator.timedStates(StubGenerator.validSequence:_*)
      val introductionDelta = LiveProcessor.empty.stateChange(
        server = server,
        previousState = ts(2),
        currentState = ts(3)
      ).value
      
      inside(introductionDelta) {
        case (Some(event), nextLive) =>
          event.name.value shouldBe "live-duel"
          nextLive.liveGames.keySet should contain only server

          val nextDelta = nextLive.stateChange(
            server = server,
            previousState = ts(3),
            currentState = ts(4)
          ).value

          // move to positive direction

          inside(nextDelta) {
            case (Some(event2), nextLive2) =>
              event2.name.value shouldBe "live-duel"
              nextLive2.liveGames.keySet should contain only server

              // and even more positive

              val nextDelta2 = nextLive2.stateChange(
                server = server,
                previousState = ts(ts.length - 2),
                currentState = ts(ts.length - 1)
              ).value

              inside(nextDelta2) {
                case (Some(event3), nextLive3) =>
                  event3.name.value shouldBe "duel"
                  nextLive3.liveGames shouldBe empty
              }
          }

          // or negative

          val discardDelta = nextLive.stateChange(
            server = server,
            previousState = ts(4),
            currentState = ZOutOfGameState
          ).value

          inside(discardDelta) {
            case (Some(event4), discardLive4) =>
              event4.name.value shouldBe "live-duel-gone"
              discardLive4.liveGames shouldBe empty
          }
      }
    }
  }
}
