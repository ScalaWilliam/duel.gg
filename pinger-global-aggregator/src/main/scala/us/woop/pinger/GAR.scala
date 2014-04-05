package us.woop.pinger

import com.datastax.driver.core.{PreparedStatement, Cluster}
import us.woop.pinger.SauerbratenServerData._
import us.woop.pinger.PingerServiceData._
import scala.util.control.NonFatal

object GAR extends App {

  val session = Cluster.builder.addContactPoints("127.0.0.1").build.connect()
  if ( session.isClosed ) {
    throw new RuntimeException("Session is closed for some reason.")
  }

  session.execute("""CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};""")

  import us.woop.pinger.persistence.TableCreation.addCreationToSession

  session.createDefaultTables

//  def someWhat[T<:Product]: PartialFunction[SauerbratenPong, Seq[(String, Seq[Any])] = {
//    case pong@SauerbratenPong(_, _, payload: T) => prepare(pong, payload)
//  }


  {

    import us.woop.pinger.persistence.InputProcessor.inputProcessor
    val sp = SauerbratenPong(1396726979,("95.85.28.218",40000),ServerInfoReply(0,259,5,549,16,None,None,"corruption","effic.me 4"))


    val inputs = List(ServerInfoReply(5, 259, 3, 451, 17, None, None, "frozen", "sauer.woop.us"),
    PlayerCns(105, List(0, 1, 4, 5, 2)),
    PlayerExtInfo(105, 0, 33, "w00p|Dr.Akkå", "good", 0, 0, 0, 0, 100, 0, 6, 3, 5, "91.121.182.x"),
    PlayerExtInfo(105, 1, 42, "king", "good", 11, 11, 0, 22, 1, 0, 4, 0, 0, "5.15.155.x"),
    PlayerExtInfo(105, 4, 49, "Uez", "good", 9, 14, 0, 20, -99, 0, 4, 0, 1, "109.29.179.x"),
    PlayerExtInfo(105, 5, 30, "GiftzZ", "good", 14, 11, 0, 28, 1, 0, 4, 0, 0, "109.192.248.x"),
    TeamScores(105, 3, 451, List()))

    val pongs = inputs map {SauerbratenPong(1231231231,("ehahe",142), _)}

    val results = pongs flatMap inputProcessor

    val queries = results map { _._1 }

    val statements = scala.collection.mutable.HashMap[String, PreparedStatement]()

    val boundStatements = results map {
      case (stmt, data) => try {
        statements.getOrElseUpdate(stmt, session.prepare(stmt)).bind(data.toSeq.map{_.asInstanceOf[Object]} : _*)
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed bind for $stmt due to $e", e)
      }
    }

    val outputs = boundStatements foreach session.execute
    println(outputs)

//
//    val result = inputProcessor apply sp
//
//    println(result)
//    val sir = ConvertedServerInfoReply.convert(ServerInfoReply(0,259,5,549,16,None,None,"corruption","effic.me 4"))
//
//    val goo = implicitly[PayloadResolver[ConvertedServerInfoReply]]
//    val q = goo.makeInsertQuery
//    println(q)
//    val b = session.prepare(q)
//    val bb = b.bind(goo.stmtValues(sp, sir) :_*)
//    session.execute(bb)

  }

//
//  val props =
//    """
//      |us.woop.pinger.pinger-service.subscribe-to-ping-delay=500ms
//      |us.woop.pinger.pinger-service.default-ping-interval=10s
//    """.stripMargin
//  implicit val ass = ActorSystem("grand", ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(props)))
//
//  val pingerService = ass.actorOf(Props(classOf[PingerService]))
//
//  import akka.actor.ActorDSL._
//
//  import scala.concurrent.future
//  def getServers: Future[Set[(String, Int)]] = future {
//    MasterserverClient.getServers(MasterserverClient.sauerMasterserver)
//  } (concurrent.ExecutionContext.global)
//
//  val mwah = actor(new Act with ActorLogging {
//
//    import context.dispatcher
//
//    val pingInterval = PingerServiceSettings.lookup().get(ass).defaultPingInterval
//
//    for {
//      servers <- getServers
//      (server, num) <- servers.zipWithIndex
//      subscribe = Subscribe(Server(server._1, server._2))
//      startIn  = (pingInterval * ((num + 1) / servers.size.toFloat)).asInstanceOf[FiniteDuration]
//    } {
//      context.system.scheduler.scheduleOnce(startIn, pingerService, subscribe)
//    }
//
//    val push: PartialFunction[BoundStatement, Unit] = {
//      case null =>
//      case s => session.executeAsync(s)
//    }
//    case object ClearBadHashLog
//    import scala.concurrent.duration._
//    context.system.scheduler.schedule(10.seconds, 1.minute, self, ClearBadHashLog)
//    val badHashAccumulator = scala.collection.mutable.Map[(String, Int), Int]().withDefaultValue{0}
//    val sauerbratenPong: PartialFunction[Any, SauerbratenPong] = {
//      case x : SauerbratenPong => x
//    }
//    val dump = sauerbratenPong andThen (persistSauerbratenResponse orElse persistPlayerExtInfo) andThen push
//    become {
//      case stuff: SauerbratenPong if persistDetails.isDefinedAt(stuff) =>
//        val hoorah = persistDetails apply stuff
//        push apply hoorah
//       stuff match {
//         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients == 0 =>
//           pingerService ! Subscribe(Server(host, port), 30.seconds)
//         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients > 8 =>
//           pingerService ! Subscribe(Server(host, port), 5.seconds)
//         case SauerbratenPong(_, (host: String, port: Int), reply: ServerInfoReply) if reply.clients > 2 =>
//           pingerService ! Subscribe(Server(host, port), 15.seconds)
//         case _ =>
//       }
//      case SauerbratenPong(_, _, _: PlayerCns) =>
//      case SauerbratenPong(_, _, _: TeamScores) =>
//      case SauerbratenPong(_, _, _: HopmodUptime) =>
//      case SauerbratenPong(_, _, _: Uptime) =>
//      case SauerbratenPong(_, _, _: ThomasExt) =>
//      case SauerbratenPong(_, server, _: OlderClient) =>
//        pingerService ! Unsubscribe(Server(server._1, server._2))
//      case SauerbratenPong(_, server, _: BadHash) =>
//        badHashAccumulator(server) = badHashAccumulator(server) + 1
//        if ( badHashAccumulator(server) > 2 ) {
//          log.warning("Service {} becoming unsubscribed because of bad hashes.", server)
//          pingerService ! Unsubscribe(Server(server._1, server._2))
//        }
//      case ClearBadHashLog =>
//        badHashAccumulator.clear()
//      case other => println(s"Not processed - $other")
//    }
//  })

}
