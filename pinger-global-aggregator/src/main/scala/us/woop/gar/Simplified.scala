package us.woop.gar

import akka.actor._
import us.woop.pinger.client.{SauerbratenProtocol, PingerClient}
import akka.actor.ActorDSL._
import us.woop.pinger.client.PingerClient._
import us.woop.pinger.PingerServiceData.Server
import scala.concurrent.duration.FiniteDuration
import us.woop.pinger.client.PingerClient.Ping
import us.woop.pinger.client.PingerClient.ParsedMessage
import us.woop.pinger.client.PingerClient.Ready
import us.woop.pinger.persistence.StatementGeneratorImplicits.ResolveServerInfoReply
import us.woop.pinger.SauerbratenServerData.Conversions.ConvertedServerInfoReply
import akka.event.LoggingReceive
import us.woop.pinger.client.PingerClient.Ping
import us.woop.pinger.client.PingerClient.BadHash
import us.woop.pinger.client.PingerClient.ParsedMessage
import us.woop.pinger.client.PingerClient.CannotParse
import us.woop.pinger.client.PingerClient.Ready
import us.woop.pinger.{MasterserverClientActor, PingerServiceData, SauerbratenServerData}
import java.io.{File, ByteArrayOutputStream, ObjectOutputStream, StringWriter}
import us.woop.pinger.MasterserverClientActor.{ServerAdded, MasterServers, RefreshServerList}
import akka.actor.SupervisorStrategy.Directive
import scala.util.control.NonFatal
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo

object Simplified extends App {
  implicit val as = ActorSystem("testSystem")
//  val targetServer = Server("212.114.59.78", 44444)
val targetServer = Server("188.226.169.46", PingerServiceData.defaultSauerbratenPort)
  sealed abstract class Pollrate(val every: FiniteDuration)

  class ServerDesc(var rate: Pollrate, var clients: Int, var respondedLast: BigInt, var cannotParse: Int, var badHashes: Int, var totalMessages: Int) {

    def reset() {
      cannotParse = 0
      badHashes = 0
      totalMessages = 0
    }

    def dump = Map('rate -> rate, 'clients -> clients, 'respondedLast -> respondedLast, 'cannotParse -> cannotParse, 'badHashes -> badHashes, 'totalMessages -> totalMessages)

  }

  class DatabaseUseException(e: Throwable) extends RuntimeException(e)
  case class StoreKey(server: (String, Int), time: Long)

  class Persister extends Act with ActorLogging {
    import org.iq80.leveldb._
    import org.fusesource.leveldbjni.JniDBFactory._
    import java.io._
    val options = new Options()
    options.createIfMissing(true)


    var db: DB = null
    whenStarting {
      try {
        db = factory.open(new File("example"), options)
        db.resumeCompactions()
      } catch {
        case e: Throwable => throw new DatabaseUseException(e)
      }
    }


    whenStopping {
      if ( db != null ) db.close()
    }

    def persist(msg: ParsedMessage) {
      try {
        db.put(s"${msg.host._1}:${msg.host._2}:${System.currentTimeMillis}".getBytes("UTF-8"), msg.recombined.toArray)
      } catch {
        case NonFatal(e) => throw new DatabaseUseException(e)
      }
    }
    become {
      case c @ ParsedMessage(host, _, content: PlayerExtInfo) =>
        persist(c)
      case c @ ParsedMessage(host, _, content: ConvertedServerInfoReply) =>
        persist(c)
    }
  }

  val ok = actor("ba")(new Act with ActorLogging {
    val yes = actor(context, "ok")(new SimplifiedController)
    superviseWith {
      val decider: PartialFunction[Throwable, Directive] = {
        case _: DatabaseUseException => Stop
      }
      OneForOneStrategy(maxNrOfRetries = 0)(decider.orElse(SupervisorStrategy.defaultStrategy.decider))
    }
  })

  class SimplifiedController extends Act with ActorLogging {

    import concurrent.duration._

    case object Stopped extends Pollrate(0.seconds)

    case object Fast extends Pollrate(3.seconds)

    case object Medium extends Pollrate(10.seconds)

    case object Slow extends Pollrate(30.seconds)

    val persister = actor(context, "persister")(new Persister)

    case class Poll(rate: Pollrate)

    val pinger = context.actorOf(Props[FullPingerClient])

    val servers = collection.mutable.HashMap[Server, ServerDesc]()

    def activeServers = servers filter {
      _._2.rate != Stopped
    }

    superviseWith {
      val decider: PartialFunction[Throwable, Directive] = {
        case _: DatabaseUseException => Escalate
      }
      OneForOneStrategy(maxNrOfRetries = 0)(decider.orElse(SupervisorStrategy.defaultStrategy.decider))
    }

    whenStarting {
      import context.dispatcher
      context.system.scheduler.schedule(5.seconds, Fast.every, self, Poll(Fast))
      context.system.scheduler.schedule(5.seconds, Medium.every, self, Poll(Medium))
      context.system.scheduler.schedule(5.seconds, Slow.every, self, Poll(Slow))
      context.system.scheduler.schedule(1.minute, 1.minute, self, 'CheckStatus)
      context.system.scheduler.schedule(5.seconds, 5.seconds, self, 'PreventSpam)
    }

    val rateRevision: PartialFunction[Int, Pollrate] = {
      case x if x > 6 => Fast
      case x if x > 2 => Medium
      case _ => Slow
    }

    object ActiveServer {
      def unapply(from: (String, Int)): Option[(Server, ServerDesc)] = {
        val server = Server(from._1, from._2)
        for {desc <- activeServers get server} yield (server, desc)
      }
    }

    val masterserverClient: ActorRef = context.actorOf(Props[MasterserverClientActor], name = "masterserverClient")

    become( {
      case Ready(_) =>
        self ! targetServer

      {
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.schedule(0.seconds, 5.minutes, masterserverClient, RefreshServerList)
      }

        become {
          case 'PreventSpam =>
            for {
              (server, desc) <- activeServers
              messagesPerSecond = desc.totalMessages / desc.rate.every.toSeconds
              if messagesPerSecond > 600
            } {
              desc.rate = Stopped
              log.warning("Server {} stopped - total {} messages (ratio {})!", server, desc.totalMessages, messagesPerSecond)
            }

          case 'CheckStatus =>
            for {
              (server, desc) <- activeServers
              if desc.totalMessages == 0
            } {
              desc.rate = Slow
              context.actorSelection("/user/monitor") ! ('ServersStatus -> desc.dump)
              desc.reset()
            }

          case Poll(rate) =>
            for {
              (server, desc) <- activeServers
              if desc.rate == rate
            } pinger ! Ping((server.ip, server.port))

          case BadHash(ActiveServer(server, desc), _) =>
            desc.badHashes = desc.badHashes + 1
            desc.respondedLast = System.currentTimeMillis()

          case CannotParse(ActiveServer(server, desc), _) =>
            desc.badHashes = desc.cannotParse + 1
            desc.respondedLast = System.currentTimeMillis()

          case m @ ParsedMessage(ActiveServer(server, desc), _, message) =>
            desc.totalMessages = desc.totalMessages + 1
            desc.respondedLast = System.currentTimeMillis()

            persister ! m

//            log.debug("Message from {}: {}", server, message)

            message match {
              case infoReply: ConvertedServerInfoReply =>
                val newRate = rateRevision(infoReply.clients)
                if (desc.rate != newRate) {
                  log.info("Changing {} rate from {} to {} ({} clients)", server, desc.rate, newRate, infoReply.clients)
                  desc.rate = newRate
                }
              case _ =>
            }

          case server: Server if servers contains server =>
            pinger ! Ping((server.ip, server.port))

          case server: Server =>
            servers += server -> new ServerDesc(Medium, 0, System.currentTimeMillis, 0, 0, 0)
            self ! server
          case MasterServers(addServers) =>
            for { server <- addServers } self ! Server(server._1, server._2)
          case ServerAdded(s) =>
            self ! Server(s._1, s._2)
        }
    })
  }

}
