package us.woop.pinger

import akka.actor.{ActorLogging, Props, ActorSystem}
import com.datastax.driver.core.{Cluster, BoundStatement}
import java.util.UUID
import scala.concurrent.Future
import us.woop.pinger.MasterserverClient
import us.woop.pinger.PingerServiceData._
import us.woop.pinger.SauerbratenServerData._
import scala.concurrent.duration.FiniteDuration
import us.woop.pinger.SauerbratenServerData.TeamScores
import us.woop.pinger.SauerbratenServerData.ServerInfoReply
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo
import us.woop.pinger.SauerbratenServerData.HopmodUptime
import us.woop.pinger.PingerClient.BadHash
import us.woop.pinger.SauerbratenServerData.TeamScores
import us.woop.pinger.SauerbratenServerData.Uptime
import us.woop.pinger.SauerbratenServerData.PlayerCns
import us.woop.pinger.PingerClient.BadHash
import us.woop.pinger.PingerServiceData.ChangeRate
import us.woop.pinger.SauerbratenServerData.OlderClient
import us.woop.pinger.PingerServiceData.Subscribe
import us.woop.pinger.SauerbratenServerData.ServerInfoReply
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.SauerbratenServerData.ThomasExt
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo
import us.woop.pinger.SauerbratenServerData.HopmodUptime

object GAR extends App {

  implicit val ass = ActorSystem("grand")

  val pingerService = ass.actorOf(Props(classOf[PingerService]))

  import akka.actor.ActorDSL._

  val session = Cluster.builder.addContactPoints("127.0.0.1").build.connect()
  session.execute("""CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};""")
  session.execute(
    """
      |CREATE TABLE IF NOT EXISTS simplex.serverinforeply (
      |id uuid,
      |serverip text, serverport int, time bigint,
      |clients int, protocol int,
      |gamemode int, remain int, maxclients int,
      |gamepaused boolean, gamespeed int,
      |mapname text, description text,
      |PRIMARY KEY(id));
    """.stripMargin)

  session.execute(
    """
      |CREATE TABLE IF NOT EXISTS simplex.playerextinfo(
      |id uuid, serverip text, serverport int, time bigint,
      |version int, cn int, ping int, name text, team text,
      |frags int, deaths int, teamkills int, accuracy int,
      |health int, armour int, gun int, privilege int,
      |state int, ip text, PRIMARY KEY(id));
    """.stripMargin)
  def persistSauerbratenResponse: PartialFunction[SauerbratenPong, BoundStatement] = {
    val stmt = session.prepare(
      """
        |INSERT INTO simplex.serverinforeply (id,
        |serverip, serverport, time, clients,
        |protocol, gamemode, remain, maxclients, gamepaused,
        |gamespeed, mapname, description) VALUES (?,
        |?, ?, ?, ?,   ?, ?, ?, ?, ?,  ?, ?, ?);
      """.stripMargin)

    {
      case SauerbratenPong(time, (host: String, port: Int), message: ServerInfoReply) =>
        stmt.bind(UUID.randomUUID.asInstanceOf[Object],
          host.asInstanceOf[Object], port.asInstanceOf[Object], time.asInstanceOf[Object],
          message.clients.asInstanceOf[Object], message.protocol.asInstanceOf[Object],
          message.gamemode.asInstanceOf[Object], message.remain.asInstanceOf[Object],
          message.maxclients.asInstanceOf[Object], (message.gamepaused.getOrElse(0) != 0).asInstanceOf[Object],
          message.gamespeed.getOrElse(100).asInstanceOf[Object],
          message.mapname.asInstanceOf[Object], message.description.asInstanceOf[Object])
    }
  }

  def persistPlayerExtInfo: PartialFunction[SauerbratenPong, BoundStatement] = {
    val stmt = session.prepare(
      """
        |INSERT INTO simplex.playerextinfo(id,
        |serverip, serverport, time, version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
        |gun, privilege, state, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
      """.stripMargin
    )

    {
      case SauerbratenPong(time, (host: String, port: Int), message: PlayerExtInfo) =>
        stmt.bind(UUID.randomUUID.asInstanceOf[Object],

          host.asInstanceOf[Object], port.asInstanceOf[Object], time.asInstanceOf[Object],
          message.version.asInstanceOf[Object], message.cn.asInstanceOf[Object], message.ping.asInstanceOf[Object],
          message.name.asInstanceOf[Object], message.team.asInstanceOf[Object], message.frags.asInstanceOf[Object],
          message.deaths.asInstanceOf[Object], message.teamkills.asInstanceOf[Object], message.accuracy.asInstanceOf[Object],
          message.health.asInstanceOf[Object], message.armour.asInstanceOf[Object], message.gun.asInstanceOf[Object],
          message.privilege.asInstanceOf[Object], message.state.asInstanceOf[Object], message.ip.asInstanceOf[Object]
        )
    }
  }

  import scala.concurrent.future
  def getServers: Future[Set[(String, Int)]] = future {
    MasterserverClient.getServers(MasterserverClient.sauerMasterserver)
  } (concurrent.ExecutionContext.global)

  val mwah = actor(new Act with ActorLogging {

    import context.dispatcher

    val pingInterval = PingerServiceSettings.lookup().get(ass).defaultPingInterval

    for {
      servers <- getServers
      (server, num) <- servers.zipWithIndex
      subscribe = Subscribe(Server(server._1, server._2))
      startIn  = (pingInterval * ((num + 1) / servers.size.toFloat)).asInstanceOf[FiniteDuration]
    } {
      context.system.scheduler.scheduleOnce(startIn, pingerService, subscribe)
    }

    val push : PartialFunction[BoundStatement, Unit] = {
      case stmt => session.executeAsync(stmt)
    }
    case object ClearBadHashLog
    import scala.concurrent.duration._
    context.system.scheduler.schedule(10.seconds, 1.minute, self, ClearBadHashLog)
    val badHashAccumulator = scala.collection.mutable.Map[(String, Int), Int]().withDefaultValue{0}
    val dump = ((persistSauerbratenResponse orElse persistPlayerExtInfo) andThen push).asInstanceOf[Receive]
    become {
      case stuff if dump.isDefinedAt(stuff) =>
       dump apply stuff
       stuff match {
         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients == 0 =>
           pingerService ! ChangeRate(Server(host, port), 30.seconds)
         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients > 8 =>
           pingerService ! ChangeRate(Server(host, port), 5.seconds)
         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients > 2 =>
           pingerService ! ChangeRate(Server(host, port), 15.seconds)
         case _ =>
       }
      case SauerbratenPong(_, _, _: PlayerCns) =>
      case SauerbratenPong(_, _, _: TeamScores) =>
      case SauerbratenPong(_, _, _: HopmodUptime) =>
      case SauerbratenPong(_, _, _: Uptime) =>
      case SauerbratenPong(_, _, _: ThomasExt) =>
      case SauerbratenPong(_, server, _: OlderClient) =>
        pingerService ! Unsubscribe(Server(server._1, server._2))
      case SauerbratenPong(_, server, _: BadHash) =>
        badHashAccumulator(server) = badHashAccumulator(server) + 1
        if ( badHashAccumulator(server) > 2 ) {
          log.warning("Service {} becoming unsubscribed because of bad hashes.", server)
          pingerService ! Unsubscribe(Server(server._1, server._2))
        }
      case ClearBadHashLog =>
        badHashAccumulator.clear()
      case other => println(other)
    }
  })

}
