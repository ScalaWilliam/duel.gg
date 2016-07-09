package gg.duel.pinger.mulprocess

import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes

case class NewProcessor(sIteratorState: SIteratorState) {

  def receiveBytes(r: ReceivedBytes): (List[Event], Option[SimpleCompletedDuel], NewProcessor) = {
    val currentState = sIteratorState.next(r.toSauerBytes)
    currentState match {
      case SFoundGame(_, CompletedGame(duel, _)) =>
        val theEvent = Event(
          data = duel.toJson,
          id = Option(duel.startTimeText),
          name = Option("duel")
        )
        (List(theEvent), Option(duel), copy(sIteratorState = currentState))
      case _ =>
        (List.empty, Option.empty, copy(sIteratorState = currentState))
    }
  }
}
object NewProcessor {
  def empty = NewProcessor(sIteratorState = SIteratorState.empty)
}
