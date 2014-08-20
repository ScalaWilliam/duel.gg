package us
import BaseXPersister.PublicDuelId
import play.api.libs.ws.WSAPI
import us.woop.pinger.analytics.DuelMaker.SimpleCompletedDuel
import us.woop.pinger.data.journal.IterationMetaData
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.xml.Elem

object BaseXPersister {

  case class PublicDuelId(value: String)

}
trait AsyncDuelPersister {
  def pushDuel(duelXml: SimpleCompletedDuel, metadata: IterationMetaData)(implicit ec: ExecutionContext): Future[PublicDuelId]
  def getDuel(duelId: PublicDuelId)(implicit ec: ExecutionContext): Future[Option[scala.xml.Elem]]
  def listDuels(implicit ec: ExecutionContext): Future[List[scala.xml.Elem]]
}
class WSAsyncDuelPersister(client: WSAPI, basexContextPath: String, dbName: String, chars: String) extends AsyncDuelPersister {
  import play.api.libs.ws._

  def createDatabase(implicit ec: ExecutionContext) = {
    postIntoRoot(
      <query xmlns="http://basex.org/rest">
        <text>db:create(&quot;{dbName}&quot;)</text>
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
      .map(filterFailed) recover {
        case NonFatal(e) =>
          throw new RuntimeException(s"Request to $url with body $xml failed due to $e", e)
      }
  }


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

  protected def pushDuelOut(newDuel: scala.xml.Elem) =
    s"""
          |$duelsAreSimilar
          |$getNewDuelId
          |$getRandomId
          |$within
          |
          |declare variable $$new-duel as node() external;
          |declare variable $$meta-data-id as xs:string external;
          |let $$new-duel := $newDuel
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

  protected def getSimilarDuel(newDuel: scala.xml.Elem) =
    s"""
          |$duelsAreSimilar
          |$within
          |
          |let $$new-duel := $newDuel
          |for $$duel in /duel
          |where local:duels-are-similar($$duel, $$new-duel)
          |return $$duel
        """.stripMargin

  protected val readDuels =s"""/duel"""

  protected lazy val listDuelsE = "/duel"
  
  protected def filterFailed(r: WSResponse) = {
    if ( r.status != 200 ) { throw new RuntimeException(s"Expected 200, got ${r.status}. Body ${r.body}") }
    r
  }
  override def pushDuel(duelXml: SimpleCompletedDuel, metadata: IterationMetaData)(implicit ec: ExecutionContext): Future[PublicDuelId] = {
    val xml = duelXml.toXml
    for {
      push <- postIntoDatabase(<query xmlns="http://basex.org/rest">
        <text>{pushDuelOut(xml)}</text>
        <variable name="meta-data-id" value={metadata.id}/>
      </query>)
      webIdResponse <- postIntoDatabase(<query xmlns="http://basex.org/rest">
        <text>{getSimilarDuel(xml)}</text>
        </query>)
    } yield PublicDuelId(webIdResponse.xml \ "@web-id" text)
  }

  override def getDuel(duelId: PublicDuelId)(implicit ec: ExecutionContext): Future[Option[Elem]] = {
    for { r <- postIntoDatabase(<query xmlns="http://basex.org/rest">
      <text><![CDATA[
        declare variable $web-id as xs:string external;
        /duel[@web-id=$web-id]
        ]]>
      </text>
      <variable name="web-id" value={duelId.value}/>
    </query>)
    } yield {
      if ( r.body.nonEmpty ) {
        Option(r.xml)
      } else {
        None
      }
    }
  }

  override def listDuels(implicit ec: ExecutionContext): Future[List[Elem]] = {
    for { r <- postIntoDatabase(<query xmlns="http://basex.org/rest">
      <text><![CDATA[
        <duels>{/duel}</duels>]]>
      </text>
    </query>)
    } yield (r.xml \ "duel").toList.map(_.asInstanceOf[Elem])
  }
}
