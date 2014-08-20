package controllers

import org.basex.examples.api.BaseXClient
import org.basex.examples.api.BaseXClient.EventNotifier
import play.api.mvc._
import play.twirl.api.Html

object Duelgg extends Controller {

  def holder = {
    import play.api.Play.current
    import play.api.libs.ws._
    WS.url("http://localhost:8984/rest/duelsz")
      .withHeaders("Accept" -> "application/xml")
      .withRequestTimeout(10000)
  }

  def getIndex = {
    holder.post("""
    <query xmlns='http://basex.org/rest'>
      <text><![CDATA[
      let $until := xs:dateTime(fn:current-dateTime() - xs:dayTimeDuration("P10D"))
      |     let $duels-items := for $duel in /duel[@web-id]
      |        let $dateTime := xs:dateTime($duel/@start-time)
      |        where $dateTime ge $until
      |        order by $dateTime descending
      |        return
      |        <li><a href="{data($duel/@web-id)}">
      |          <header>
      |            <h2>{data($duel/players/player[1]/@name)} vs {data($duel/players/player[2]/@name)}</h2>
      |            <h3>{data($duel/@mode)} @ {data($duel/@map)}</h3>
      |          </header>
      |          <footer>
      |            <p class="score">{data($duel/players/player[1]/@frags)}-{data($duel/players/player[2]/@frags)}</p>
      |            <p class="when">{fn:format-dateTime($dateTime,
      |                 "[Y01]/[M01]/[D01]")}</p>
      |          </footer></a>
      |        </li>
      |
      |        return fn:subsequence($duels-items, 1, 10)
      ]]></text></query>""".stripMargin
    )
  }

  def getDuel(duelId: String) = {
    val xqueryText =
    """
      |declare variable $web-id as xs:string external;
      |for $duel in /duel[@web-id=$web-id]
      |        let $dateTime := xs:dateTime($duel/@start-time)
      |return
      |<html>
      |<head><title>{data($duel/players/player[1]/@name)} vs {data($duel/players/player[2]/@name)}</title>
      |    <link rel="stylesheet" href="/assets/stylesheets/main.css" type="text/css"/></head>
      |<body>
      |
      |<header id="top">
      |    <h1><a href="/">Duel? GG!</a></h1>
      |</header>
      |<article class="duel">
      |    <header>
      |        <h3>{data($duel/@mode)} @@ {data($duel/@map)}</h3>
      |        <h2>{fn:format-dateTime($dateTime,
      |                 "[Y01]/[M01]/[D01]")}</h2>
      |    </header>
      |    <section class="duel">
      |        <section class="score score-left">
      |            <p class="score">{data($duel/players/player[1]/@frags)}</p>
      |            <p class="name">{data($duel/players/player[1]/@name)}</p>
      |        </section>
      |        <section class="score score-right">
      |            <p class="score">{data($duel/players/player[2]/@frags)}</p>
      |            <p class="name">{data($duel/players/player[2]/@name)}</p>
      |        </section>
      |    </section>
      |</article>
      |</body>
      |</html>
    """.stripMargin
    holder.post(<query xmlns="http://basex.org/rest">
      <text>{xqueryText}</text>
      <variable name="web-id" value={duelId}/>
      </query>
    )
  }

  lazy val eventClient = {
    val b = new BaseXClient("127.0.0.1", 1984, "admin", "admin")
    b.execute("CHECK duelsz")
    b.watch("new-duels", new EventNotifier {
      override def notify(value: String): Unit = {
        println(s"Received event $value")
      }

    })
    b
  }

  def index = Action.async {
    request =>
      eventClient.hashCode()
      import scala.concurrent.ExecutionContext.Implicits.global
      getIndex.map(_.body).map(x => Ok(views.html.index(Html(x))))
  }
  def showPage(id: String) = Action.async {
    request =>
      import scala.concurrent.ExecutionContext.Implicits.global
      getDuel(id).map(_.body).map(x => Ok(Html(x)))
  }
}