package us.woop.pinger.bobby

import scala.concurrent.{Future, ExecutionContext}

object QueryDuel {

  import spray.http._
  import spray.client.pipelining._

  def apply(requestResponse: HttpRequest => Future[HttpResponse])(dbPath: String)(duelId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    for {
      response <- requestResponse(Post(dbPath, duelQuery(duelId)))
    } yield {
      for {
        r <- Option(response)
        body = r.entity.asString
        if body.contains("duel.gg")
      } yield body
    }
  }

  def duelQuery(duelId: String) = <query xmlns="http://basex.org/rest">
    <text><![CDATA[
declare variable $duel-id as xs:string external;
for $duel in subsequence(db:open("db-stage")/duel[@web-id=$duel-id], 1, 1)
let $mapmode := (data($duel/@map), " @ ", data($duel/@mode))
let $a := ($duel/players/player)[1]
let $b := ($duel/players/player)[2]
let $names := (data($a/@name), " vs ", data($b/@name))
let $scores := (data($a/@frags), ":", data($b/@frags))
let $web-url := ("http://duel.gg/", data($duel/@web-id))
return string-join(($names, " - ", $mapmode, " - ", $scores , " - ", $web-url), "")
]]></text>
    <parameter name="method" value="text"/>
    <variable name="duel-id" value={duelId}/>
  </query>

}
