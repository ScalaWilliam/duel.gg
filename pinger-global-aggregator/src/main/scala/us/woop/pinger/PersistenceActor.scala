package us.woop.pinger

import akka.actor.ActorDSL._
import com.datastax.driver.core.{Cluster, Session, PreparedStatement}
import us.woop.pinger.PingerServiceData.SauerbratenPong
import scala.util.control.NonFatal
import akka.actor.ActorLogging
import us.woop.pinger.PingerClient.BadHash
import us.woop.pinger.WoopMonitoring.MonitorMessage

class PersistenceActor extends Act with ActorLogging with WoopMonitoring {

  import us.woop.pinger.persistence.TableCreation.addCreationToSession
  import us.woop.pinger.persistence.InputProcessor.inputProcessor
  import scala.concurrent.duration._

  val statements = collection.mutable.HashMap[String, PreparedStatement]()
  var session: Session = null

  /** Map: Host -> Message class -> Count **/
  val inserts = collection.mutable.ListBuffer[((String, Int), String)]()

  case object FlushMetrics

  whenStarting {
    session = Cluster.builder.addContactPoints("127.0.0.1").build.connect()
    if (session.isClosed) {
      throw new RuntimeException("Session is closed for some reason.")
    }
    session.execute( """CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};""")
    session.createDefaultTables
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(5.seconds, 10.seconds, self, FlushMetrics)
  }

  become {
    case FlushMetrics =>
      sendMetrics('TypeCounts, inserts.groupBy{_._2}.mapValues{_.size}.toMap)
      sendMetrics('ServerCounts, inserts.groupBy{_._1}.mapValues{_.size}.toMap)
      inserts.clear()

    case message: SauerbratenPong if inputProcessor.isDefinedAt(message) =>
      for {
        (cqlStatement, data) <- inputProcessor apply message
      } yield try {
        val statement = statements.getOrElseUpdate(cqlStatement, session.prepare(cqlStatement))
        val objectData = data.map{_.asInstanceOf[Object]}
        val boundStatement = statement.bind(objectData : _*)
        session.executeAsync(boundStatement)
        val className = message.payload.getClass.getName.split("\\$|\\.").last
        inserts += message.host -> className
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed execute for $cqlStatement due to $e", e)
      }
    case SauerbratenPong(_,_,_: BadHash) =>
    case other =>
      log.debug("Received unhandled message to persister: {}", other)
  }

}
