package us
import java.io.{ByteArrayInputStream, StringWriter}
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xquery.XQItemType
import BaseXPersister.PublicDuelId
import com.xqj2.XQConnection2
import us.woop.pinger.analytics.DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.journal.IterationMetaData
import scala.util.control.NonFatal
import scala.xml.Elem

object BaseXPersister {

  case class PublicDuelId(value: String)

  val dbf = DocumentBuilderFactory.newInstance

  object Implicits {
    implicit class XmlElemToNode(val elem: scala.xml.Elem) extends AnyVal {
      def asJava = xmlElemToNode(elem)
    }
    implicit class NodeToXmlElem(val node: org.w3c.dom.Node) extends AnyVal {
      def asScala = nodeToXmlElem(node)
    }
  }
  private def xmlElemToNode(xml: scala.xml.Elem): org.w3c.dom.Node = {
    val builder = dbf.newDocumentBuilder
    val xmlStr = s"$xml"
    val result = builder.parse(new ByteArrayInputStream(xmlStr.getBytes))
    result.getDocumentElement
  }

  private def nodeToXmlElem(node: org.w3c.dom.Node) = {
    val writer = new StringWriter()
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.transform(new DOMSource(node), new StreamResult(writer))
    val xml = writer.toString
    try {
      scala.xml.XML.loadString(xml)
    } catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed to parse $xml", e)
    }
  }

}
trait BaseXPersister {
  def pushDuel(duelXml: SimpleCompletedDuel, metadata: IterationMetaData): PublicDuelId
  def getDuel(duelId: PublicDuelId): Option[scala.xml.Elem]
  def listDuels: List[scala.xml.Elem]
}
class SimpleBaseXPerister(dbName: String, database: XQConnection2, chars: String) extends BaseXPersister {
  import BaseXPersister.Implicits._
  protected val within =
      """
        |declare function local:within($first as xs:dateTime, $second as xs:dateTime, $maxInterval as xs:dayTimeDuration) {
        |  let $zero := xs:dayTimeDuration("PT0S")
        |  let $smf := $second - $first
        |  let $fms := $first - $second
        |  return (
        |    ($smf ge $zero) and ($smf lt $maxInterval)
        |  ) or ( ($fms ge $zero ) and ($fms lt $maxInterval))
        |};
        |"""

  protected val getRandomId =
      """declare function local:get-random-id($chars as xs:string) {
        |  let $length := string-length($chars)
        |  let $new-chars :=
        |    for $i in 1 to $length
        |    let $idx := 1 + random:integer($length)
        |    return substring($chars, $idx, 1)
        |  return string-join($new-chars)
        |};""".stripMargin

  protected val getNewDuelId =
      """declare function local:get-new-duel-id($duels as node()*, $chars as xs:string) {
        |  let $length := string-length($chars)
        |  let $new-id := local:get-random-id($chars)
        |  return
        |    if (empty($duels[@web-id = $new-id]))
        |    then ($new-id)
        |    else (local:get-new-duel-id($duels, $chars))
        |};""".stripMargin

  protected  val duelsAreSimilar =
      """
        |declare function local:duels-are-similar($a as node(), $b as node()) {
        |  (
        |    $a/@server eq $b/@server
        |  ) and (
        |    local:within(
        |      xs:dateTime($a/@start-time),
        |      xs:dateTime($b/@start-time),
        |      xs:dayTimeDuration("PT5M")
        |    )
        |  )
        |};
      """.stripMargin

  protected val q =
      s"""
          |$duelsAreSimilar
          |$getNewDuelId
          |$getRandomId
          |$within
          |
          |declare variable $$new-duel as node() external;
          |declare variable $$meta-data-id as xs:string external;
          |
          |let $$new-duel-id := local:get-new-duel-id(/duel, "$chars")
          |let $$similar-duels :=
          |  for $$duel in /duel
          |  where local:duels-are-similar($$duel, $$new-duel)
          |  return data($$duel/@web-id)
          |let $$updated-duel :=
          |  copy $$updated-duel := $$new-duel
          |  modify (
          |    rename node $$updated-duel as 'duel',
          |    insert node (attribute {'web-id'} { $$new-duel-id }) into $$updated-duel
          |  )
          |  return $$updated-duel
          |return
          |  if ( empty($$similar-duels) )
          |  then (db:add("$dbName", $$updated-duel, $$meta-data-id), db:event("new-duels", "WUT") )
          |  else (db:event("new-duels", "WUT?"))
        """.stripMargin

  protected lazy val getSimilarDuel = database.prepareExpression(
      s"""
          |$duelsAreSimilar
          |$within
          |
          |declare variable $$new-duel as node() external;
          |for $$duel in /duel
          |where local:duels-are-similar($$duel, $$new-duel)
          |return $$duel
        """.stripMargin
    )
  protected lazy val readDuels = database.prepareExpression(
      s"""
           |/duel
         """.stripMargin)
  protected lazy val pushDuelOut = database.prepareExpression(q)

    protected lazy val listDuelsE = database.prepareExpression(
      s"""
           |/duel
         """.stripMargin
    )

    override def pushDuel(duelXml: SimpleCompletedDuel, metadata: IterationMetaData): PublicDuelId = {
      val xml = duelXml.toXml
      pushDuelOut.bindNode(new QName("new-duel"), xml.asJava, null)
      pushDuelOut.bindString(new QName("meta-data-id"), metadata.id, null)
      pushDuelOut.executeQuery().close()

      getSimilarDuel.bindNode(new QName("new-duel"), xml.asJava, null)
      val res = getSimilarDuel.executeQuery()
      val duelId = try {
        if (!res.next()) {
          throw new IllegalStateException("Query returned no result, expected to have a result!")
        }
        val str = res.getNode.asScala
        val webId = (str \ "@web-id").text
        PublicDuelId(webId)
      } finally {
        res.close()
      }
      duelId
    }

    override def getDuel(duelId: PublicDuelId): Option[Elem] = {
      val res = readDuels.executeQuery()
      try {
        if (res.next()) {
          Option(res.getNode.asScala)
        } else {
          None
        }
      } finally {
        res.close()
      }
    }

    override def listDuels: List[Elem] = {
      val result = listDuelsE.executeQuery()
      try {
        Iterator.continually(result.next()).takeWhile(identity).map {
          _ =>
            result.getNode.asScala
        }.toList
      } finally {
        result.close()
      }
    }
  }
