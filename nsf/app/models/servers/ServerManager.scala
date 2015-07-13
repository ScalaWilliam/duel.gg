package models.servers

import javax.inject._

import akka.agent.Agent
import gg.duel.pinger.data.Server
import models.SimpleEmbeddedDatabase
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
 * Created on 13/07/2015.
 */
@Singleton
class ServerManager @Inject()(simpleEmbeddedDatabase: SimpleEmbeddedDatabase)
                              (implicit
                               executionContext: ExecutionContext,
                               applicationLifecycle: ApplicationLifecycle
                                ) {

  import collection.JavaConverters._
  val theMap = simpleEmbeddedDatabase.get().openMap[String, String]("servers")

  def getFromMap = Servers{
    theMap.entrySet().asScala.flatMap{ e =>
      Try(e.getKey -> Server.fromAddress(e.getKey)).toOption
    }.toMap
  }

  val serversA: Agent[Servers] = Agent(getFromMap)

  def servers: Servers = serversA.get()

  def addServer(name: String, server: Server): Unit = {
    theMap.put(name, server.getAddress)
    simpleEmbeddedDatabase.commit()
    serversA.send(_.include(name, server))
  }

  def deleteServer(name: String): Unit = {
    theMap.remove(name)
    simpleEmbeddedDatabase.commit()
    serversA.send(_.exclude(name))
  }

}
