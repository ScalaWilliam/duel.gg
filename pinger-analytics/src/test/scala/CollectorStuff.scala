import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import com.xqj2.XQConnection2
import net.xqj.basex.BaseXXQDataSource
import us.woop.pinger.analytics.actor.BaseXPersisterGuardianActor

object CollectorStuff extends App {
  def basexConnection = {
    val xqs = new BaseXXQDataSource() {
      setProperty("serverName", "localhost")
      setProperty("port", "1984")
      setProperty("databaseName", "dang")
    }
    xqs.getConnection("admin", "admin").asInstanceOf[XQConnection2]
  }
  val context = ActorSystem("lulz")
  val gameCollectorPersister = actor(context, name = "gameCollectorPersister")(new BaseXPersisterGuardianActor(basexConnection))

}
