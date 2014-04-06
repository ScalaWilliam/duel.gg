import com.datastax.driver.core.{PreparedStatement, Cluster}
import org.scalatest.{Matchers, WordSpec}
import scala.util.control.NonFatal
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.SauerbratenServerData.{TeamScores, PlayerExtInfo, PlayerCns, ServerInfoReply}

class AssertWorking extends WordSpec with Matchers {

  val session = Cluster.builder.addContactPoints("127.0.0.1").build.connect()
  if ( session.isClosed ) {
    throw new RuntimeException("Session is closed for some reason.")
  }

  session.execute("""CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};""")

  import us.woop.pinger.persistence.TableCreation.addCreationToSession

  session.createDefaultTables

  {

    import us.woop.pinger.persistence.InputProcessor.inputProcessor
    val sp = SauerbratenPong(1396726979, ("95.85.28.218", 40000), ServerInfoReply(0, 259, 5, 549, 16, None, None, "corruption", "effic.me 4"))

    val inputs = List(ServerInfoReply(5, 259, 3, 451, 17, None, None, "frozen", "sauer.woop.us"),
      PlayerCns(105, List(0, 1, 4, 5, 2)),
      PlayerExtInfo(105, 0, 33, "w00p|Dr.Akkå", "good", 0, 0, 0, 0, 100, 0, 6, 3, 5, "91.121.182.x"),
      PlayerExtInfo(105, 1, 42, "king", "good", 11, 11, 0, 22, 1, 0, 4, 0, 0, "5.15.155.x"),
      PlayerExtInfo(105, 4, 49, "Uez", "good", 9, 14, 0, 20, -99, 0, 4, 0, 1, "109.29.179.x"),
      PlayerExtInfo(105, 5, 30, "GiftzZ", "good", 14, 11, 0, 28, 1, 0, 4, 0, 0, "109.192.248.x"),
      TeamScores(105, 3, 451, List()))

    val pongs = inputs map {
      SauerbratenPong(1231231231, ("ehahe", 142), _)
    }

    val results = pongs flatMap inputProcessor

    val queries = results map {
      _._1
    }

    val statements = scala.collection.mutable.HashMap[String, PreparedStatement]()

    val boundStatements = results map {
      case (stmt, data) => try {
        statements.getOrElseUpdate(stmt, session.prepare(stmt)).bind(data.toSeq.map {
          _.asInstanceOf[Object]
        }: _*)
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed bind for $stmt due to $e", e)
      }
    }

    val outputs = boundStatements foreach session.execute
    println(outputs)
  }
}
