package gg.duel.pinger.app

import java.io.File
import java.net.InetAddress

import akka.actor.ActorSystem
import com.maxmind.geoip2.DatabaseReader
import spray.client.pipelining._
import spray.http.{BasicHttpCredentials, HttpHeaders}

import scala.concurrent.Await
import scala.util.Try

object SetupPartialIpsApp extends App {
  implicit val as = ActorSystem("testSystem")
  import scala.concurrent.ExecutionContext.Implicits.global
  val pipeline = spray.client.pipelining.sendReceive

  def lookupCountryByIp(ip: String): Option[String] = {
    for {
      country <- Option(reader.country(InetAddress.getByName(ip)))
      code <- Option(country.getCountry.getIsoCode)
    } yield code
  }
  def lookupCountryByPartialIp(ip: String): Option[String] = {
        Try(lookupCountryByIp(ip.replaceAllLiterally("x","1"))).toOption.flatten
//    lookupCountryByIp(ip.replaceAllLiterally("x","1"))
  }

  val reader = {
    val database = new File(scala.util.Properties.userHome, "GeoLite2-Country.mmdb")
    new DatabaseReader.Builder(database).build()
  }
println("starting...")
  val res = pipeline(Post("http://admin:admin@odin.duel.gg:3238/rest/db-stage",
   <rest:query xmlns:rest="http://basex.org/rest">
    <rest:text><![CDATA[
distinct-values(//@partial-ip)
]]>
    </rest:text>
    <rest:variable name="game-id" value="2015-01-29T01:21:18Z::85.214.66.181:20000"/>
  </rest:query>).withHeaders(
      HttpHeaders.Authorization(BasicHttpCredentials("admin", "admin"))))

  import concurrent.duration._
  val out = for { stuff <- res }
  yield {
    println("Got it!")
    val ipToCountry = for {
      partialIp <- stuff.entity.asString.split("\r?\n").toIterator
      countryCode <- lookupCountryByPartialIp(partialIp)
    } yield <partial-ip partial-ip={partialIp} country-code={countryCode}/>
    val query = <rest:query xmlns:rest="http://basex.org/rest">
      <rest:text><![CDATA[
for $player in db:open("db-stage")//player
where $player/@partial-ip and not($player/@country-code)
for $pip in //partial-ip
where $pip/@partial-ip = data($player/@partial-ip)
let $country-code := data($pip/@country-code)
return insert node (attribute country-code {$country-code}) into $player
]]>
      </rest:text>
      <rest:context><ips>{ipToCountry.toList}</ips></rest:context>
    </rest:query>
    val res = pipeline(Post("http://admin:admin@odin.duel.gg:3238/rest/db-stage",
    query).withHeaders(
        HttpHeaders.Authorization(BasicHttpCredentials("admin", "admin"))))
    res
  }

  val result = out.flatMap(identity)
  result onComplete {
    case x => println(s"Completed! With $x")
  }
}