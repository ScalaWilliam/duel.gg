package us
import us.BaseXPersister.{PublicCtfId, MetaId, PublicDuelId}
import play.api.libs.ws.WSAPI
import us.ServerRetriever.ServersList
import us.woop.pinger.analytics.CTFGameMaker.SimpleCompletedCTF
import us.woop.pinger.analytics.worse.DuelMaker
import DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.Server
import us.woop.pinger.data.journal.IterationMetaData
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal
import scala.xml.{PCData, Elem}

object BaseXPersister {

  case class PublicDuelId(value: String)
  case class PublicCtfId(value: String)
  case class MetaId(value: String)

}
trait AsyncGamePersister extends AsyncCtfPersister with AsyncDuelPersister
trait AsyncCtfPersister {
  def pushCtf(duelXml: SimpleCompletedCTF)(implicit ec: ExecutionContext): Future[Unit]

  def getCtf(duelId: PublicCtfId)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]

  def getSimilarCtf(ctf: SimpleCompletedCTF)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]

  def listCtfs(implicit ec: ExecutionContext): Future[List[scala.xml.Elem]]
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
object ServerRetriever {
  case class ServerListing(connect: String, server: Server, alias: String, active: Boolean)
  case class ServersList(active: List[ServerListing], inactive: List[ServerListing])
}
trait ServerRetriever {
  self: BaseXClient =>

  def deactivateServer(connect: String)(implicit ec: ExecutionContext): Future[Unit] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[
      declare variable $connect as xs:string external;
for $server in /server[@connect = $connect and not(@inactive)]
return insert node (attribute {"inactive"} {"true"}) into $server
]]></text>
        <variable name="connect" value={connect}/>
      </query>).map(_ => ())
  }

  def activateServer(connect: String)(implicit ec: ExecutionContext): Future[Unit] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[
      declare variable $connect as xs:string external;
for $server in /server[@connect = $connect]
return delete node $server/@inactive
]]></text>
        <variable name="connect" value={connect}/>
      </query>).map(_ => ())
  }

  def changeServerAlias(connect: String, newAlias: String)(implicit ec: ExecutionContext): Future[Unit] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[
      declare variable $connect as xs:string external;
      declare variable $newAlias as xs:string external;
for $server in /server[@connect = $connect]
return replace value of node $server/@alias with $newAlias
]]></text>
        <variable name="connect" value={connect}/>
        <variable name="newAlias" value={newAlias}/>
      </query>).map(_ => ())
  }

  def addServer(connect: String, alias: String)(implicit ec: ExecutionContext): Future[Unit] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[


      declare variable $connect as xs:string external;
      declare variable $alias as xs:string external;
if ( empty(/server[@connect = $connect]) ) then (
db:add("]]>{dbName}<![CDATA[", <server connect="{$connect}" alias="{$alias}"/>, "bxp-servers")
) else ()
]]></text>
        <variable name="connect" value={connect}/>
        <variable name="alias" value={alias}/>
      </query>).map(_ => ())
  }

  def retrieveServers(implicit ec: ExecutionContext): Future[ServerRetriever.ServersList] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[
<servers>{
/server[@connect]
}</servers>
]]></text>
      </query>
    ).map(doc => {
      val listOfServers = for {
        // parallel because we'll be getting DNS
        serverXml <- (doc.xml \ "server").par
        connect <- serverXml \ "@connect" map (_.text)
        alias <- serverXml \ "@alias" map (_.text)
        isActive = !(serverXml \ "@inactive").exists(_.text == "true")
        sauerServer <- Try(Server.fromAddress(connect)).toOption.toList
      } yield ServerRetriever.ServerListing(connect, sauerServer, alias, isActive)
      val (active, inactive) = listOfServers.toList.partition(_.active)
      ServersList(active.toList, inactive.toList)
    }
    )
  }
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

class WSAsyncGamePersister(val client: WSAPI, val basexContextPath: String, val dbName: String, val chars: String)
  extends AsyncGamePersister
  with BaseXClient
  with ServerRetriever
  with MetaPersister
{

  lazy val functions = {
    import scalax.io.JavaConverters._
    this.getClass.getResource("/functions.xqm").asInput.string
  }


  override def pushCtf(ctfXml: SimpleCompletedCTF)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml = ctfXml.toXml
    postIntoDatabase(
      <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text>{PCData(functions +
          s"""
            |let $$ctx := /completed-ctf
            |let $$server := data($$ctx/@server)
            |let $$map := data($$ctx/@map)
            |let $$mode := data($$ctx/@mode)
            |let $$exist-no-matches := empty(((db:open("$dbName")/ctf)[@server = $$server][@mode = $$mode][@map = $$map][local:ctfs-are-similar(., $$ctx)]))
            |return
            |if ( $$exist-no-matches )
            |then (
            | let $$new-ctf-id := local:get-new-ctf-id(db:open("$dbName")/ctf, "$chars")
            | return local:add-new-ctf($$new-ctf-id, "$dbName", $$ctx)
            |)
            |else ()
            |""".stripMargin
        )}</rest:text>
        <rest:context>{xml}</rest:context>
      </rest:query>
    ).map(x => ())
  }

  override def getCtf(ctfId: PublicCtfId)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabase(
      <query xmlns="http://basex.org/rest">
        <text>{PCData(functions + s"""
      declare variable $$ctf-id as xs:string external;
      (db:open("$dbName")/ctf)[@web-id=$$ctf-id]]
      """)}</text>
        <variable name="ctf-id" value={ctfId.value}/>
      </query>
    ).map(x =>
      if ( x.body.nonEmpty ) Some(x.xml) else None
      )
  }

  override def listCtfs(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabase(<query xmlns="http://basex.org/rest">
      <text>{PCData(functions + s"""<ctfs>{db:open("$dbName")/ctf}</ctfs>""")}</text>
    </query>).map(_.xml \ "ctf").map(_.toList.map(_.asInstanceOf[Elem]))
  }

  override def getSimilarCtf(ctfDefinition: SimpleCompletedCTF)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabase(<rest:query xmlns:rest="http://basex.org/rest">
      <rest:text>{PCData(functions +
        s"""let $$ctx := /completed-ctf
         |let $$server := data($$ctx/@server)
         |let $$mode := data($$ctx/@mode)
         |let $$map := data($$ctx/@map)
         |return db:open("$dbName")/ctf[@server = $$server][@map = $$map][@mode = $$mode][local:ctfs-are-similar(., $$ctx)]""".stripMargin)}</rest:text>
      <rest:context>{ctfDefinition.toXml}</rest:context>
    </rest:query>).map(x => if ( x.body.nonEmpty) Some(x.xml) else None)
  }

  override def pushDuel(duelXml: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml = duelXml.toXml
    postIntoDatabase(
      <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text>{PCData(functions +
          s"""
            |let $$ctx := /completed-duel
            |let $$server := data($$ctx/@server)
            |let $$map := data($$ctx/@map)
            |let $$mode := data($$ctx/@mode)
            |let $$exist-no-matches := empty(((db:open("$dbName")/duel)[@server = $$server][@mode = $$mode][@map = $$map][local:duels-are-similar(., $$ctx)]))
            |return
            |if ( $$exist-no-matches )
            |then (
            | let $$new-duel-id := local:get-new-duel-id(db:open("$dbName")/duel, "$chars")
            | return local:add-new-duel($$new-duel-id, "$dbName", $$ctx)
            |)
            |else ()
            |""".stripMargin
        )}</rest:text>
        <rest:context>{xml}</rest:context>
      </rest:query>
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
    postIntoDatabase(<rest:query xmlns:rest="http://basex.org/rest">
    <rest:text>{PCData(functions +
      s"""let $$ctx := /completed-duel
         |let $$server := data($$ctx/@server)
         |let $$mode := data($$ctx/@mode)
         |let $$map := data($$ctx/@map)
         |return db:open("$dbName")/duel[@server = $$server][@map = $$map][@mode = $$mode][local:duels-are-similar(., $$ctx)]""".stripMargin)}</rest:text>
      <rest:context>{duelDefinition.toXml}</rest:context>
    </rest:query>).map(x => if ( x.body.nonEmpty) Some(x.xml) else None)
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
