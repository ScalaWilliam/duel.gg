package us.woop.pinger.analytics.actor
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import com.xqj2.XQConnection2
import us.woop.pinger.analytics.actor.data.IndividualGameCollectorActor.HaveGame
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.namespace.QName
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult

class BaseXPersisterActor(conn: XQConnection2) extends Act with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global
  import concurrent.duration._

  case object Heartbeat
  context.system.scheduler.schedule(5.seconds, 5.seconds, self, Heartbeat)

  whenStarting {
    log.info("BaseX persistence started up")
  }

  whenStopping {
    log.info("BaseX persistence shut down")
  }

  become {
    case game: HaveGame =>
      val item = conn.createItemFromDocument(game.data, null, null)
      val gamePath = s"/games/${System.currentTimeMillis}.xml"
      conn.insertItem(gamePath, item, null)
      log.info("Recorded a new game at {} for server {}", gamePath, game.server)
    case Heartbeat =>
      conn.createExpression().executeQuery("1")
  }

}

object BaseXPersisterActor {

  object Conversions {
    private val builderFactory = DocumentBuilderFactory.newInstance()
    private val tFactory = javax.xml.transform.TransformerFactory.newInstance
    implicit def scalaXmlToDom(node: scala.xml.Node) =
      builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(node.buildString(stripComments = false))))
    implicit def toQName(name: String): QName = new QName(name)

    implicit def toScala(dom: org.w3c.dom.Node): Node = {
      val adapter = new NoBindingFactoryAdapter
      tFactory.newTransformer().transform(new DOMSource(dom), new SAXResult(adapter))
      adapter.rootElem
    }
  }
}