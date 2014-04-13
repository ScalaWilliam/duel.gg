package us.woop.pinger.analytics.persistence
import net.xqj.basex.BaseXXQDataSource
import javax.xml.namespace.QName
import com.xqj2.XQConnection2
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object ToBasex extends App {

  val conn = {
    val xqs = new BaseXXQDataSource() {
      setProperty("serverName", "localhost")
      setProperty("port", "1984")
      setProperty("databaseName", "dang")
    }
    xqs.getConnection("admin", "admin").asInstanceOf[XQConnection2]
  }

  val xqpe = {
    val xqpe = conn.prepareExpression("declare variable $x as xs:string external; $x")
    xqpe.bindString(new QName("x"), "Hello World!", null)
    xqpe
  }
  val seq = xqpe.executeQuery()
  import collection.JavaConverters._
  while (seq.next())
    System.out.println(seq.getItemAsString(null))

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
  import Conversions._
  val nono:scala.xml.Node = <hey/>
  val game = new { val data = "<test/>" }
  val item = conn.createItemFromDocument(game.data, null, null)
  conn.insertItem(s"/games/${System.currentTimeMillis}.xml", item, null)
  game.data

//  conn.insertItem("/recorded-games/hehehe.xml", conn.createItemFromNode(nono, null), null)
  conn.close()

}
