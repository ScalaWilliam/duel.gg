package us
import us.BaseXPersister.{MetaId, PublicDuelId}
import play.api.libs.ws.WSAPI
import us.woop.pinger.analytics.DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.journal.IterationMetaData
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.xml.{PCData, Elem}

object BaseXPersister {

  case class PublicDuelId(value: String)
  case class MetaId(value: String)

}
trait AsyncDuelPersister {
  def pushDuel(duelXml: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Unit]

  def getDuel(duelId: PublicDuelId)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]

  def getSimilarDuel(duelId: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]

  def listDuels(implicit ec: ExecutionContext): Future[List[scala.xml.Elem]]
}
trait MetaPersister {
  def pushMeta(metaXml: IterationMetaData)(implicit ec: ExecutionContext): Future[Unit]
  def getMeta(metaId: MetaId)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]
  def listMetas(implicit ec: ExecutionContext): Future[List[scala.xml.Elem]]
}

trait BaseXClient {
  import play.api.libs.ws._
  def client: WSAPI
  def dbName: String
  def basexContextPath: String

  def createDatabase(implicit ec: ExecutionContext) = {
    postIntoRoot(
      <query xmlns="http://basex.org/rest">
        <text>if ( not(db:exists(&quot;{dbName}&quot;)) ) then (db:create(&quot;{dbName}&quot;)) else ()</text>
      </query>
    ).map(_.body)
  }

  def dropDatabase(implicit ec: ExecutionContext) = {
    postIntoRoot(
      <query xmlns="http://basex.org/rest">
        <text>db:drop(&quot;{dbName}&quot;)</text>
      </query>
    ).map(_.body)
  }

  protected def postIntoRoot(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    postXml(s"$basexContextPath/rest")(xml)
  }

  protected def postIntoDatabase(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    postXml(s"$basexContextPath/rest/$dbName")(xml)
  }

  protected def postXml(url: String)(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    client.url(url)
      .withHeaders("Accept" -> "application/xml")
      .withRequestTimeout(10000)
      .post(xml)
      .map{r =>
      if ( r.status != 200 ) { throw new RuntimeException(s"Expected 200, got ${r.status}. Body ${r.body}") }
      r} recover {
      case NonFatal(e) =>
        throw new RuntimeException(s"Request to $url with body $xml failed due to $e", e)
    }
  }


}

class WSAsyncDuelPersister(val client: WSAPI, val basexContextPath: String, val dbName: String, val chars: String) extends AsyncDuelPersister with BaseXClient

with MetaPersister {

  lazy val functions = {
    import scalax.io.JavaConverters._
    this.getClass.getResource("/functions.xqm").asInput.string
  }

  override def pushDuel(duelXml: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml = duelXml.toXml
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text>{PCData(functions +
          s"""
            |
            |if ( empty(((db:open("$dbName")/duel)[local:duels-are-similar(., $xml)])) )
            |then (
            | let $$new-duel-id := local:get-new-duel-id(db:open("$dbName")/duel, "$chars")
            | return local:add-new-duel($$new-duel-id, "$dbName", $xml)
            |)
            |else ()
            |""".stripMargin
        )}</text>
      </query>
    ).map(x => ())
  }

  override def getDuel(duelId: PublicDuelId)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
      <text>{PCData(functions + s"""
      declare variable $$duel-id as xs:string external;
      (db:open("$dbName")/duel)[@web-id=$$duel-id]]
      """)}</text>
        <variable name="duel-id" value={duelId.value}/>
      </query>
    ).map(x =>
      if ( x.body.nonEmpty ) Some(x.xml) else None
    )
  }

  override def listDuels(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabase(<query xmlns="http://basex.org/rest">
    <text>{PCData(functions + s"""<duels>{db:open("$dbName")/duel}</duels>""")}</text>
    </query>).map(_.xml \ "duel").map(_.toList.map(_.asInstanceOf[Elem]))
  }

  override def getSimilarDuel(duelDefinition: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabase(<query xmlns="http://basex.org/rest">
    <text>{PCData(functions + s"""(db:open("$dbName")/duel)[local:duels-are-similar(., ${duelDefinition.toXml})]""")}</text>
    </query>).map(x => if ( x.body.nonEmpty) Some(x.xml) else None)
  }

  override def pushMeta(metaXml: IterationMetaData)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml = metaXml.toXml
    postIntoDatabase(
    <query xmlns="http://basex.org/rest">
    <text>{PCData(
      s"""
        |declare variable $$meta-id as xs:string external;
        |if ( not(exists((db:open("$dbName")/meta)[@id = $$meta-id])) )
        |then (db:add("$dbName", $xml, "meta"))
        |else ()
        |
      """.stripMargin)}</text>
      <variable name="meta-id" value={metaXml.id}/>
    </query>
    ).map(x => ())
  }

  override def getMeta(metaId: MetaId)(implicit ec: ExecutionContext): Future[Option[Elem]] = {

    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text>{PCData(s"""
      declare variable $$meta-id as xs:string external;
      (db:open("$dbName")/meta)[@id=$$meta-id]
      """)}</text>
        <variable name="meta-id" value={metaId.value}/>
      </query>
    ).map(x =>
      if ( x.body.nonEmpty ) Some(x.xml) else None
      )
  }

  override def listMetas(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabase(<query xmlns="http://basex.org/rest">
      <text>{PCData(s"""<metas>{db:open("$dbName")/meta}</metas>""")}</text>
    </query>).map(_.xml \ "meta").map(_.toList.map(_.asInstanceOf[Elem]))
  }
}
