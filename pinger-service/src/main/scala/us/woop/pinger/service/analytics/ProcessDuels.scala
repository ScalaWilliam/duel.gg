package us.woop.pinger.service.analytics
import akka.actor.ActorDSL._
import us.woop.pinger.analytics.MultiplexedDuelReader.{SInitial, SFoundGame, SIteratorState}
import us.woop.pinger.data.journal.MetaData
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes

object ProcessDuels {

}
class ProcessDuels extends Act with ActWithStash {

  def stated(state: SIteratorState, metaData: MetaData): Receive = {
    case newMetaData: MetaData =>
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
    case m: MetaData =>
      become(stated(SInitial, m))
      unstashAll()
    case r: ReceivedBytes =>
      stash()
  }

}
