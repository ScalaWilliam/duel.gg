package us.woop.pinger.service.analytics
import akka.actor.ActorDSL._
import us.woop.pinger.analytics.better.BetterMultiplexedReader.{SInitial, SFoundGame, SIteratorState}
import us.woop.pinger.data.journal.IterationMetaData
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes

object ProcessGames {
}
class ProcessGames extends Act with ActWithStash {
  def stated(state: SIteratorState, metaData: IterationMetaData): Receive = {
    case newMetaData: IterationMetaData =>
      become(stated(state, newMetaData))

    case r: ReceivedBytes =>
      val nextState = state.next(r.toSauerBytes)
      nextState match {
        case SFoundGame(_, completedGame) =>
          val updatedGame = completedGame.copy(
            metaId = Option(metaData.id),
            game=completedGame.game.left.map(_.copy(metaId = Option(metaData.id))).right.map(_.copy(metaId = Option(metaData.id)))
          )
          context.parent ! updatedGame
          updatedGame.game.left.foreach(context.parent ! _)
          updatedGame.game.right.foreach(context.parent ! _)
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