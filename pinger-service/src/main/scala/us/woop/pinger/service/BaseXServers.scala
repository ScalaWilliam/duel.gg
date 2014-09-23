package us.woop.pinger.service
import akka.actor.ActorDSL._
import akka.actor.Props
import us.ServerRetriever.ServersList
import us.WSAsyncDuelPersister
import us.woop.pinger.service.BaseXServers.RefreshServers
import us.woop.pinger.service.PingerController.{Unmonitor, Monitor}

object BaseXServers {
  case object RefreshServers
  def props(persister: WSAsyncDuelPersister) = Props(classOf[BaseXServers], persister)
}
class BaseXServers(persister: WSAsyncDuelPersister) extends Act {

  import scala.concurrent.ExecutionContext.Implicits.global
  whenStarting {
    import concurrent.duration._
    context.system.scheduler.schedule(0.seconds, 30.seconds, self, RefreshServers)
  }

  become {
    case RefreshServers =>
      import akka.pattern.pipe
      persister.retrieveServers pipeTo self
    case ServersList(good, bad) =>
      good foreach { s => context.parent ! Monitor(s.server) }
      bad foreach { s => context.parent  ! Unmonitor(s.server) }
  }

}
