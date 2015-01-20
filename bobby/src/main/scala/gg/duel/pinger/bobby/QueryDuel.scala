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
for $duel in subsequence(/duel[@web-id=$duel-id], 1, 1)
let $server-aliases := subsequence((/server[@server = $duel/@server]/@alias, $duel/@server), 1, 1)
let $mapmode := (data($duel/@mode), " @ ", data($duel/@map))
let $a := ($duel/players/player)[1]
let $b := ($duel/players/player)[2]
let $a-text := data($a/@name) || " (" || data($a/@frags) || ")"
let $b-text := data($b/@name) || " (" || data($b/@frags) ||")"

let $bold := bin:hex("02")
let $normal := bin:hex("0F")
let $players := bin:join((
  $bold,
  bin:encode-string($a-text, "UTF-8"),
  $normal,
  bin:encode-string(" vs ", "UTF-8"),
  $bold,
  bin:encode-string($b-text, "UTF-8"),
  $normal
))
let $web-url := ("http://duel.gg/", data($duel/@web-id))
let $rest := string-join((" · ", $mapmode, " · ", $server-aliases, " · ", $web-url), "")
return bin:join(($players, bin:encode-string($rest, "UTF-8")))
]]></text>
    <parameter name="method" value="raw"/>
    <variable name="duel-id" value={duelId}/>
  </query>

}
