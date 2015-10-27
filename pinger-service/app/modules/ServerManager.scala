package modules

import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import gg.duel.pinger.data.Server
import gg.duel.pinger.masterserver.MasterserverClient
import models.servers.Servers
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created on 13/07/2015.
 */
@Singleton
class ServerManager @Inject()()(implicit
                               executionContext: ExecutionContext,
                                applicationLifecycle: ApplicationLifecycle,
                                actorSystem: ActorSystem
                                ) {

  val serversA: Agent[Servers] = Agent(Servers.empty)
  val updateServers = {
    import concurrent.duration._
    actorSystem.scheduler.schedule(0.seconds, 5.minutes) {
      serversA.send(
        newValue = Servers(
          servers = MasterserverClient.default.getServers.map { case (h, p) =>
            s"$h $p" -> Server(ip = h, port = p)
          }.toMap
        ))
    }
  }

  def servers: Servers = serversA.get()

  applicationLifecycle.addStopHook(() => Future {
    updateServers.cancel()
  })

}
