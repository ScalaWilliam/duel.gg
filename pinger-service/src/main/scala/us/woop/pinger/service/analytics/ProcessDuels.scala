package us.woop.pinger.service.analytics
import akka.actor.ActorDSL._
import us.woop.pinger.analytics.better.BetterMultiplexedReader.{SInitial, SFoundGame, SIteratorState}
import us.woop.pinger.data.journal.IterationMetaData
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes

object ProcessDuels {
}
class ProcessDuels extends Act with ActWithStash {
  def stated(state: SIteratorState, metaData: IterationMetaData): Receive = {
    case newMetaData: IterationMetaData =>
      become(stated(state, newMetaData))

    case r: ReceivedBytes =>
      val nextState = state.next(r.toSauerBytes)
      nextState match {
        case SFoundGame(_, completedDuel) =>
          context.parent ! completedDuel.copy(metaId = Option(metaData.id))
        case _ =>
      }
      become(stated(nextState, metaData))
  }

  become {
    case m: IterationMetaData =>
      become(stated(SInitial, m))
      unstashAll()
    case r: ReceivedBytes =>
      stash()
  }

}