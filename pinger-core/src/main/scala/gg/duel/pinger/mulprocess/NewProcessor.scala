package gg.duel.pinger.mulprocess

import gg.duel.pinger.analytics.MultiplexedReader.{CompletedGame, SFoundGame, SIteratorState}
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes

case class NewProcessor(sIteratorState: SIteratorState) {

  def receiveBytes(r: ReceivedBytes): (List[Event], Option[SimpleCompletedDuel], Option[SimpleCompletedCTF], NewProcessor) = {
    val currentState = sIteratorState.next(r.toSauerBytes)
    currentState match {
      case SFoundGame(_, CompletedGame(Left(duel), _)) =>
        val theEvent = Event(
          data = duel.toJson,
          id = Option(duel.startTimeText),
          name = Option("duel")
        )
        (List(theEvent), Option(duel), Option.empty, copy(sIteratorState = currentState))
      case SFoundGame(_, CompletedGame(Right(ctf), _)) =>
        val theEvent = Event(
          data = ctf.toJson,
          id = Option(ctf.startTimeText),
          name = Option("ctf")
        )
        (List(theEvent), Option.empty, Option(ctf), copy(sIteratorState = currentState))
      case _ =>
        (List.empty, Option.empty, Option.empty, copy(sIteratorState = currentState))
    }
  }
}
object NewProcessor {
  def empty = NewProcessor(sIteratorState = SIteratorState.empty)
}