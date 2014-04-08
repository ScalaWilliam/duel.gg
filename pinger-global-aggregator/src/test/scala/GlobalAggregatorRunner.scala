import akka.util.ByteString
import com.datastax.driver.core.{BoundStatement, Metadata, SocketOptions, Cluster}
import java.net.{InetSocketAddress, InetAddress}
import java.util.UUID
import scala.util.Try
import us.woop.pinger.client.SauerbratenFormat

object GlobalAggregatorRunner extends App {

  val session = Cluster.builder.addContactPoints("127.0.0.1").build.connect()

  import us.woop.pinger.PingerServiceData._

  session.execute("""CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};""")

  // create songs and playlist tables

  import us.woop.pinger.SauerbratenServerData._
  //session.execute("""DROP TABLE IF EXISTS simplex.playerextinfo;""")
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

  val persistSauerbratenResponse: PartialFunction[SauerbratenPong, BoundStatement] = {
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

  val persistPlayerExtInfo: PartialFunction[SauerbratenPong, BoundStatement] = {
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
        message.health.asInstanceOf[Object], message.armour.asInstanceOf[Object]
      )
    }
  }

//  val inp = ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
//    1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
//  )
//  val good = Try(SauerbratenProtocol.matchers apply inp.toList)
//
//  val ha = persistSauerbratenResponse apply SauerbratenPong(123412412, ("123", 124), good.get)
//
//  val result = session.execute(ha)
//  println(result)

  val ah = persistPlayerExtInfo apply SauerbratenPong(1231238999, ("1243", 514), PlayerExtInfo(105, 0, 33, "w00p|Dr.Akkå", "good", 0, 0, 0, 0, 100, 0, 6, 3, 5, "91.121.182.x"))


  println(session.execute(ah))


//
//  case class PlayerCns(version: Int, cns: List[Int])
//
//  case class PlayerExtInfo(version: Int, cn: Int, ping: Int, name: String, team: String, frags: Int,
//                           deaths: Int, teamkills: Int, accuracy: Int, health: Int, armour: Int, gun: Int, privilege: Int, state: Int, ip: String)
//
//  case class TeamScores(version: Int, gamemode: Int, remain: Int, scores: List[TeamScore])
//
//  case class TeamScore(name: String, score: Int, baseMap: Boolean, baseScores: List[Int])

}
