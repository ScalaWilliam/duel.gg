package gg.duel.pinger.service

import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.service.BaseXPersister.{PublicCtfId, MetaId, PublicDuelId}
import gg.duel.pinger.service.ServerRetriever.ServersList
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.journal.IterationMetaData
import spray.http.{HttpRequest, HttpResponse}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}
import scala.util.control.NonFatal
import scala.xml.{XML, Elem, PCData}

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
trait DemoChecker {
  self: BaseXClient =>
  def getDemoLink(gameId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val xml =
      <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text><![CDATA[
        declare variable $game-id as xs:string external;
         data(/available-demo[@simple-id = $game-id]/@link)
         ]]></rest:text>
        <rest:variable name="game-id" value={gameId}/>
      </rest:query>
    postIntoDatabase(xml).map { resp =>
      Option(resp.entity.asString).filter(_.nonEmpty)
    }
  }
  def downloadedDemo(gameId: String, fromUri: String, toFile: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml =
      <rest:query xmlns:rest="http://basex.org/rest">
        <rest:text><![CDATA[
        declare variable $game-id as xs:string external;
        declare variable $from-uri as xs:string external;
        declare variable $to-file as xs:string external;
        let $exists-already := exists(/downloaded-demo[@game-id=$game-id])
        let $new-node := <downloaded-demo game-id="{$game-id}" from-uri="{$from-uri}" to-file="{$to-file}"/>
        return if ( $exists-already ) then () else (
         db:add("]]>{dbName}<![CDATA[", $new-node, "downloaded-demos")
       )
         ]]></rest:text>
        <rest:variable name="game-id" value={gameId}/>
        <rest:variable name="from-uri" value={fromUri}/>
        <rest:variable name="to-file" value={toFile}/>
      </rest:query>
    postIntoDatabase(xml).map(_ => ())
  }
  def checkDemo(gameId: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val xml= <rest:query xmlns:rest="http://basex.org/rest">
      <rest:text><![CDATA[
declare variable $game-id as xs:string external;
let $servers := ('PSL.sauerleague.org 1', 'PSL.sauerleague.org 2', 'PSL.sauerleague.org 3', 'PSL.sauerleague.org 4')
let $servers := <servers>
<server ogros-name='PSL.sauerleague.org 1' duelgg-id='85.214.66.181:10000'/>
<server ogros-name='PSL.sauerleague.org 2' duelgg-id='85.214.66.181:20000'/>
<server ogros-name='PSL.sauerleague.org 3' duelgg-id='85.214.66.181:30000'/>
<server ogros-name='PSL.sauerleague.org 4' duelgg-id='85.214.66.181:40000'/>
<server effic-name='s1' duelgg-id='188.226.136.111:10000'/>
<server effic-name='s2' duelgg-id='188.226.136.111:20000'/>
<server effic-name='s3' duelgg-id='188.226.136.111:30000'/>
<server effic-name='s4' duelgg-id='188.226.136.111:40000'/>
<server effic-name='s5' duelgg-id='188.226.136.111:50000'/>
<server effic-name='s5' duelgg-id='188.226.136.111:60000'/>
</servers>
for $duel in (/duel, /ctf)[@simple-id=$game-id]
let $demos :=
  for $server in $servers/server[@duelgg-id = data($duel/@server)]
  return if ( $server/@effic-name ) then (
    let $uri := 'http://effic.me/demos/'||data($server/@effic-name)||'/'
    let $request := <http:request href='{$uri}' method='get'/>
    let $parsed := http:send-request($request)[2]
    for $li in $parsed//li
    let $fn := data($li/@data-href)
    let $link := $uri || $fn
    let $date := '20' || replace(substring-before($fn, '.'), '_', '-') || 'T' || replace(substring-before(substring-after($fn, '.'),'.'), '_', ':') || ':00'
    let $mode := replace(substring-before(substring($fn, 16), '.'), '_', ' ')
    let $map := substring-before(substring-after(substring($fn, 16), '.'), '.')
    where $date castable as xs:dateTime
    let $dateTime := xs:dateTime($date)
    return <demo server='{data($server/@duelgg-id)}' date="{$dateTime}" mode="{$mode}" map="{$map}" link="{$link}"/>
  ) else if ( $server/@ogros-name ) then (
    let $request :=
      <http:request href='http://ogros.org/server/demos.php' method='post'>
      <http:header name="Referer" value="http://ogros.org/server/demos.php"/>
      <http:body media-type='application/x-www-form-urlencoded' method="text">results=&amp;timezone=0&amp;server={replace(data($server/@ogros-name), ' ', '+')}</http:body>
    </http:request>
    let $response := http:send-request($request)
    for $demo in $response//tr
    for $dateV in $demo/td[1]
    for $link in data($demo/td[4][a = 'Download']/a/@href)
    let $dateT := replace(data($demo/td[1]), ' ', 'T')
    where $dateT castable as xs:dateTime
    let $dateTime := xs:dateTime($dateT)
    let $mode := data($demo/td[2])
    let $map  := data($demo/td[3])
    return <demo server='{data($server/@duelgg-id)}' date="{$dateTime}" mode="{$mode}" map="{$map}" link="{$link}"/>
  ) else ()
let $earliest-demo :=
  let $demo-dates :=
    for $demo in $demos
    where $demo/@date gt string(xs:dateTime("2015-01-01T00:00:00Z"))
    return xs:string(data($demo/@date))
  return string(xs:dateTime(min($demo-dates)) - xs:dayTimeDuration("PT1H"))
where $duel/@start-time gt string($earliest-demo)
for $demo in $demos
where $duel/@server = $demo/@server
where $duel/@mode = $demo/@mode
where $duel/@map = $demo/@map
let $earlier := string(xs:dateTime(data($demo/@date)) - xs:dayTimeDuration("PT3M"))
let $later := string(xs:dateTime(data($demo/@date)) + xs:dayTimeDuration("PT3M"))
where $duel/@start-time lt $later
where $duel/@start-time gt $earlier
let $available-demo :=
  copy $nd := $demo
  modify (
    insert node (attribute simple-id {data($duel/@simple-id)}) into $nd,
    rename node $nd as 'available-demo'
  )
  return $nd
let $existing-node := db:open("]]>{dbName}<![CDATA[")/available-demo[@simple-id = data($available-demo/@simple-id)]
return if ( not(empty($existing-node)) ) then () else (db:add("]]>{dbName}<![CDATA[", $available-demo, "demo-availability"))
]]>
      </rest:text>
      <rest:variable name="game-id" value={gameId}/>
    </rest:query>
    postIntoDatabase(xml).map(_ => ())
  }
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
    import scala.async.Async.{async, await}
    async {
      val result = await { Future { Try(Server.fromAddress(connect)) } }
      result match {
        case Success(_) =>
          await(postIntoDatabase(
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
            </query>))
        case _ => ()
      }
    }
  }

  def retrieveServers(implicit ec: ExecutionContext): Future[ServerRetriever.ServersList] = {
    postIntoDatabaseGetXmlO(
      <query xmlns="http://basex.org/rest">
        <text><![CDATA[
<servers>{
/server[@connect]
}</servers>
]]></text>
      </query>
    ).map(x => {
      val listOfServers = for {
        // parallel because we'll be getting DNS
        doc <- x.toList
        serverXml <- doc \ "server"
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
  def client: HttpRequest => Future[HttpResponse]
  def dbName: String
  def basexContextPath: String

  def connects(implicit ec: ExecutionContext) = {
  for { r <- postIntoDatabase(<rest:query xmlns:rest="http://basex.org/rest">
    <rest:text><![CDATA[db:add("]]>{dbName}<![CDATA[", <test/>, "test")]]></rest:text>
  </rest:query>)
  } yield ()

  }

  def createDatabase(implicit ec: ExecutionContext) = {
    postIntoRoot(
      <query xmlns="http://basex.org/rest">
        <text>if ( not(db:exists(&quot;{dbName}&quot;)) ) then (db:create(&quot;{dbName}&quot;)) else ()</text>
      </query>
    ).map(_.entity.asString)
  }

  def dropDatabase(implicit ec: ExecutionContext) = {
    postIntoRoot(
      <query xmlns="http://basex.org/rest">
        <text>db:drop(&quot;{dbName}&quot;)</text>
      </query>
    ).map(_.entity.asString)
  }

  protected def postIntoRoot(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    postXml(s"$basexContextPath/rest")(xml)
  }

  def postIntoDatabase(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    postXml(s"$basexContextPath/rest/$dbName")(xml)
  }
  def postIntoDatabaseGetXmlO(xml: scala.xml.Elem)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postXml(s"$basexContextPath/rest/$dbName")(xml).map(x =>
      Try(scala.xml.XML.loadString(x.entity.asString)).toOption
    )
  }

  protected def postXml(url: String)(xml: scala.xml.Elem)(implicit ec: ExecutionContext) = {
    import spray.can.Http
    import spray.http._
    import spray.client.pipelining._
    val request = Post(url, xml)
      .withHeaders(
        HttpHeaders.Authorization(BasicHttpCredentials("admin", "admin")),
        HttpHeaders.Accept(MediaRange.apply(MediaTypes.`application/xml`))
      )
    client(request).map{r =>
      if ( r.status.isSuccess ) { r } else {
        throw new RuntimeException(s"Expected a successful response, got $r")
      }
    }.recoverWith{case NonFatal(e) => throw new RuntimeException(s"Request to $url failed due to $e, body $xml due to: $e")}
  }

}

class WSAsyncGamePersister(val client: HttpRequest => Future[HttpResponse], val basexContextPath: String, val dbName: String, val chars: String)
  extends AsyncGamePersister
  with BaseXClient
  with ServerRetriever
  with MetaPersister
with DemoChecker
{

  lazy val functions: String = {
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
            |let $$start-time := data($$ctx/@start-time)
            |let $$matching-ctfs :=
            |  for $$ctf in db:open("$dbName")/ctf
            |  where $$ctf/@server = $$server
            |  where $$ctf/@mode = $$mode
            |  where $$ctf/@map = $$map
            |  where local:within-time(xs:dateTime($$ctf/@start-time), xs:dateTime($$start-time), xs:dayTimeDuration("PT5M"))
            |  return $$ctf
            |let $$exist-no-matches := empty($$matching-ctfs)
            |return
            |  if ( $$exist-no-matches )
            |  then (
            |   let $$new-ctf-id := local:get-new-ctf-id(db:open("$dbName")/ctf, "$chars")
            |   return local:add-new-ctf($$new-ctf-id, "$dbName", $$ctx)
            |  )
            |  else ()
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
      if ( x.entity.asString.nonEmpty ) Some(scala.xml.XML.loadString(x.entity.asString)) else None
      )
  }

  override def listCtfs(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabaseGetXmlO(<query xmlns="http://basex.org/rest">
      <text>{PCData(functions + s"""<ctfs>{db:open("$dbName")/ctf}</ctfs>""")}</text>
    </query>).map(_.toList.flatMap(_\"ctf").map(_.asInstanceOf[Elem]))
  }

  override def getSimilarCtf(ctfDefinition: SimpleCompletedCTF)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabase(<rest:query xmlns:rest="http://basex.org/rest">
      <rest:text>{PCData(functions +
        s"""let $$ctx := /completed-ctf
         |let $$server := data($$ctx/@server)
         |let $$mode := data($$ctx/@mode)
         |let $$map := data($$ctx/@map)
         |let $$start-time := data($$ctx/@start-time)
         |for $$ctf in db:open("$dbName")/ctf
         |where $$ctf/@server = $$server
         |where $$ctf/@mode = $$mode
         |where $$ctf/@map = $$map
         |where local:within-time(xs:dateTime($$ctf/@start-time), xs:dateTime($$start-time), xs:dayTimeDuration("PT5M"))
         |return $$ctf
         """.stripMargin)}</rest:text>
      <rest:context>{ctfDefinition.toXml}</rest:context>
    </rest:query>).map(x => if ( x.entity.asString.nonEmpty) Some(XML.loadString(x.entity.asString)) else None)
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
            |let $$start-time := data($$ctx/@start-time)
            |let $$existing-similar-duels :=
            |  for $$duel in db:open("$dbName")/duel
            |  where $$duel/@server = $$server
            |  where $$duel/@mode = $$mode
            |  where $$duel/@map = $$map
            |  where local:within-time(xs:dateTime($$duel/@start-time), xs:dateTime($$start-time), xs:dayTimeDuration("PT5M"))
            |  return $$duel
            |let $$exist-no-matches := empty($$existing-similar-duels)
            |return
            |  if ( $$exist-no-matches )
            |  then (
            |    let $$new-duel-id := local:get-new-duel-id(db:open("$dbName")/duel, "$chars")
            |    return local:add-new-duel($$new-duel-id, "$dbName", $$ctx)
            |  ) else ()
            |""".stripMargin
        )}</rest:text>
        <rest:context>{xml}</rest:context>
      </rest:query>
    ).map(x => ())
  }

  override def getDuel(duelId: PublicDuelId)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabaseGetXmlO(
      <query xmlns="http://basex.org/rest">
      <text>{PCData(functions + s"""
      declare variable $$duel-id as xs:string external;
      (db:open("$dbName")/duel)[@web-id=$$duel-id]]
      """)}</text>
        <variable name="duel-id" value={duelId.value}/>
      </query>
    )
  }

  override def listDuels(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabaseGetXmlO(<query xmlns="http://basex.org/rest">
    <text>{PCData(functions + s"""<duels>{db:open("$dbName")/duel}</duels>""")}</text>
    </query>).map(_.toList.flatMap(_ \ "duel").map(_.asInstanceOf[Elem]))
  }

  override def getSimilarDuel(duelDefinition: SimpleCompletedDuel)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    postIntoDatabaseGetXmlO(<rest:query xmlns:rest="http://basex.org/rest">
    <rest:text>{PCData(functions +
      s"""let $$ctx := /completed-duel
         |let $$server := data($$ctx/@server)
         |let $$mode := data($$ctx/@mode)
         |let $$map := data($$ctx/@map)
         |let $$start-time := data($$ctx/@start-time)
         |for $$duel in db:open("$dbName")/duel
         |where $$duel/@server = $$server
         |where $$duel/@map = $$map
         |where $$duel/@mode = $$mode
         |where local:within-time(xs:dateTime($$duel/@start-time), xs:dateTime($$start-time), xs:dayTimeDuration("PT5M"))
         |return $$duel
         |
         |""".stripMargin)}</rest:text>
      <rest:context>{duelDefinition.toXml}</rest:context>
    </rest:query>)
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

    postIntoDatabaseGetXmlO(
      <query xmlns="http://basex.org/rest">
        <text>{PCData(s"""
      declare variable $$meta-id as xs:string external;
      (db:open("$dbName")/meta)[@id=$$meta-id]
      """)}</text>
        <variable name="meta-id" value={metaId.value}/>
      </query>
    )
  }

  override def listMetas(implicit ec: ExecutionContext): Future[List[Elem]] = {
    postIntoDatabaseGetXmlO(<query xmlns="http://basex.org/rest">
      <text>{PCData(s"""<metas>{db:open("$dbName")/meta}</metas>""")}</text>
    </query>).map(_.toList.flatMap(_ \ "meta").map(_.asInstanceOf[Elem]))
  }
}
