package plugins

import plugins.DuelStoragePlugin.{Duel, User}

import scala.annotation.tailrec
import scala.async.Async.{async, await}
import play.api._
import plugins.DataSourcePlugin._
import scala.collection.mutable
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.xml.{PCData, Text}

object DataSourcePlugin {

  case class UserProfile(name: String, profileData: String)
  def plugin: DataSourcePlugin = Play.current.plugin[DataSourcePlugin]
    .getOrElse(throw new RuntimeException("DataSourcePlugin plugin not loaded"))

}
class DataSourcePlugin(implicit app: Application) extends Plugin {
  import scala.concurrent.ExecutionContext.Implicits.global
  def getPlayers = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
        let $lis := for $ru in /registered-user
        order by $ru/@id ascending
        return <li><a href="/player/{data($ru/@id)}/">{data($ru/@game-nickname)}</a></li>
        return <article id="players-list"><ol>{$lis}</ol></article>
        ]]></rest:text></rest:query>).map(_.body)
  }

  def getServers = {
    BasexProviderPlugin.awaitPlugin.query(<query xmlns="http://basex.org/rest">
          <text><![CDATA[
          <ul>{
          for $server in /server[@connect and not(@inactive)]
          order by $server/@connect ascending
          let $alias := data($server/@alias)
          let $connect := data($server/@connect)
          let $txt := if ( $alias ) then ($alias || " (" ||$connect||")") else ($connect)
          return <li>{$txt}</li>
          }</ul>
]]></text></query>).map(_.body)
  }


  def queryUsers = {
    async {
      val a = await(BasexProviderPlugin.awaitPlugin.query{
        <rest:query xmlns:rest="http://basex.org/rest"><rest:text><![CDATA[
        <users>{/registered-user}</users>
        ]]></rest:text></rest:query>})
      val users = a.xml \ "registered-user" collect { case e: scala.xml.Elem => User.fromXml(e) }
      users.toVector
    }
  }
  def queryUser(id: String) = {
    async {
      val a = await(BasexProviderPlugin.awaitPlugin.query{
        <rest:query xmlns:rest="http://basex.org/rest"><rest:text><![CDATA[
        declare variable $id as xs:string external;
        <users>{/registered-user[@id=$id]}</users>
        ]]></rest:text><rest:variable name="id" value={id}/></rest:query>})
      val users = a.xml \ "registered-user" collectFirst { case e: scala.xml.Elem => User.fromXml(e) }
      users
    }
  }
  def queryGame(id: Int) = {
    async {
      val a = await(BasexProviderPlugin.awaitPlugin.query {
        <rest:query xmlns:rest="http://basex.org/rest">
          <rest:text>
            <![CDATA[
            declare variable $id as xs:string external;
            <duels>{(/materialised-duel)[@id = $id]}</duels>
          ]]>
          </rest:text>
          <rest:variable name="id" value={id.toString}/>
        </rest:query>
      })
      val duels = a.xml \ "materialised-duel" collect { case e: scala.xml.Elem => Duel.fromXml(e)}
      duels.headOption
    }
  }
  def queryGames(ids: Set[Int]) = {
    async {
      val a = await(BasexProviderPlugin.awaitPlugin.query {
        <rest:query xmlns:rest="http://basex.org/rest">
          <rest:text>
            <![CDATA[
            declare variable $ids as xs:string external;
            <duels>{(/materialised-duel)[@id = tokenize($ids, " ")]}</duels>
          ]]>
          </rest:text>
          <rest:variable name="ids" value={ids.mkString(" ")}/>
        </rest:query>
      })
      val duels = a.xml \ "materialised-duel" collect { case e: scala.xml.Elem => Duel.fromXml(e)}
      duels.toVector
    }
  }
  def queryGames(fromPos: Int, toPos: Int) = {
    async {
      val a = await(BasexProviderPlugin.awaitPlugin.query {
        <rest:query xmlns:rest="http://basex.org/rest">
          <rest:text>
            <![CDATA[
declare variable $from-pos as xs:int  external;
declare variable $to-pos as xs:int external;
<duels>{
 (/materialised-duel)[position() = $from-pos to $to-pos]
(:
  for $duel in (/duel)[position() = $from-pos to $to-pos]
  let $first-map := map {
    "id": data($duel/@int-id),
    "atTime": adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ()),
    "leftPlayerScore": data($duel/player[1]/@frags),
    "leftPlayerName": data($duel/player[1]/@name),
    "rightPlayerScore": data($duel/player[2]/@frags),
    "rightPlayerName": data($duel/player[2]/@name),
    "mode": data($duel/@mode),
    "map": data($duel/@map)
  }
  let $score-log :=
    let $times :=
      for $t in 0 to data($duel/@duration)
      order by $t ascending
      return $t
    let $player-times :=
      for $player in $duel/player
      return array {
        for $t in $times
        let $frags := if ($t = 0) then (0) else ((xs:int(data($player/frags[@at = $t])), '-')[1])
        return $frags
      }
    return $player-times
  let $second-map := for $ru in /registered-user[nickname = data($duel/player[1]/@name)]/@id return map { "leftPlayerId": data($ru) }
  let $third-map := for $ru in /registered-user[nickname = data($duel/player[2]/@name)]/@id return map { "rightPlayerId": data($ru) }
  let $json := json:serialize(map:merge(($first-map, $second-map, $third-map, map { "scoreLog": array { $score-log } } )))
  let $unix-time := (xs:dateTime(data($duel/@start-time)) - xs:dateTime("1970-01-01T00:00:00-00:00")) div xs:dayTimeDuration('PT0.001S')
  return <duel id="{data($duel/@int-id)}" at="{$unix-time}" nicknames="{data($duel/player/@name) => string-join(" ")}" json="{$json}"/>:)
}</duels>
]]>
          </rest:text>
          <rest:variable name="from-pos" value={fromPos.toString}/>
          <rest:variable name="to-pos" value={toPos.toString}/>
        </rest:query>
      })
      val duels = a.xml \ "materialised-duel" collect { case e: scala.xml.Elem => Duel.fromXml(e)}
      duels.toVector
    }
  }
  def gamesZ: Future[Vector[Duel]] = {
    val increment = 500
    def go(startingPos: Int, accum: Vector[Duel]): Future[Vector[Duel]] = {
      val finishPos = startingPos + increment - 1
      async {
        val res = await(queryGames(startingPos, finishPos))
        if ( res.isEmpty ) accum
        else await(go(startingPos + increment, accum ++ res))
      }
    }
    go(0, Vector.empty)
  }

  def duelsWithoutMaterialisedVersions: Future[Vector[Int]] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest="http://basex.org/rest"><rest:text><![CDATA[
let $duels := data(/duel/@int-id)
let $mduels := data(/materialised-duel/@id)
return $duels[not(.=$mduels)]
]]>
    </rest:text>
    </rest:query>).map(_.body.split("\n").map(_.toInt).toVector)
  }

  def materialiseGames(ids: Set[Int]): Future[Unit] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest="http://basex.org/rest"><rest:text><![CDATA[
declare variable $list-of-ids as xs:string external;
  let $rematerialise-ids := $list-of-ids => tokenize(' ')
  for $duel in /duel[@int-id = $rematerialise-ids]
  let $existing-material := /materialised-duel[@id = $duel/@int-id]
  let $first-map := map {
    "id": data($duel/@int-id),
    "atTime": adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ()),
    "leftPlayerScore": data($duel/player[1]/@frags),
    "leftPlayerName": data($duel/player[1]/@name),
    "rightPlayerScore": data($duel/player[2]/@frags),
    "rightPlayerName": data($duel/player[2]/@name),
    "mode": data($duel/@mode),
    "map": data($duel/@map)
  }
  let $score-log :=
    let $times :=
      for $t in 0 to data($duel/@duration)
      order by $t ascending
      return $t
    let $player-times :=
      for $player in $duel/player
      return array {
        for $t in $times
        let $frags := if ($t = 0) then (0) else ((xs:int(data($player/frags[@at = $t])), '-')[1])
        return $frags
      }
    return $player-times
  let $left-player := for $ru in /registered-user[nickname = data($duel/player[1]/@name)] return data($ru/@id)
  let $right-player := for $ru in /registered-user[nickname = data($duel/player[2]/@name)] return data($ru/@id)
  let $second-map := for $id in $left-player return map { "leftPlayerId": $id }
  let $third-map := for $id in $right-player return map { "rightPlayerId": $id }
  let $json := json:serialize(map:merge(($first-map, $second-map, $third-map, map { "scoreLog": array { $score-log } } )))
  let $unix-time := (xs:dateTime(data($duel/@start-time)) - xs:dateTime("1970-01-01T00:00:00-00:00")) div xs:dayTimeDuration('PT0.001S')
  let $new-node := <materialised-duel id="{data($duel/@int-id)}" at="{$unix-time}" updated-at="{current-dateTime()}" users="{($left-player, $right-player) => string-join(" ")}" nicknames="{data($duel/player/@name) => string-join(" ")}" json="{$json}"/>
  return if ( exists($existing-material) ) then (replace node $existing-material with $new-node) else (db:add("duelgg", $new-node, "materialised-duels"))

]]>
    </rest:text>
      <rest:variable name="list-of-ids" value={ids.mkString(" ")}/>
    </rest:query>
    ).map(_ => ())
  }

  def materialiseUnmaterialisedGames: Future[Unit] = {
    BasexProviderPlugin.awaitPlugin.query(<rest:query xmlns:rest="http://basex.org/rest"><rest:text><![CDATA[
let $ggs :=
  let $duels := data(/duel/@int-id)
  let $mduels := data(/materialised-duel/@id)
  let $non-materialised := $duels[not(.=$mduels)]
  for $duel in /duel[@int-id = $non-materialised]
  let $first-map := map {
    "id": data($duel/@int-id),
    "atTime": adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ()),
    "leftPlayerScore": data($duel/player[1]/@frags),
    "leftPlayerName": data($duel/player[1]/@name),
    "rightPlayerScore": data($duel/player[2]/@frags),
    "rightPlayerName": data($duel/player[2]/@name),
    "mode": data($duel/@mode),
    "map": data($duel/@map)
  }
  let $score-log :=
    let $times :=
      for $t in 0 to data($duel/@duration)
      order by $t ascending
      return $t
    let $player-times :=
      for $player in $duel/player
      return array {
        for $t in $times
        let $frags := if ($t = 0) then (0) else ((xs:int(data($player/frags[@at = $t])), '-')[1])
        return $frags
      }
    return $player-times
  let $left-player := for $ru in /registered-user[nickname = data($duel/player[1]/@name)] return data($ru/@id)
  let $right-player := for $ru in /registered-user[nickname = data($duel/player[2]/@name)] return data($ru/@id)
  let $second-map := for $id in $left-player return map { "leftPlayerId": $id }
  let $third-map := for $id in $right-player return map { "rightPlayerId": $id }
  let $json := json:serialize(map:merge(($first-map, $second-map, $third-map, map { "scoreLog": array { $score-log } } )))
  let $unix-time := (xs:dateTime(data($duel/@start-time)) - xs:dateTime("1970-01-01T00:00:00-00:00")) div xs:dayTimeDuration('PT0.001S')
  let $new-node := <materialised-duel id="{data($duel/@int-id)}" at="{$unix-time}" users="{($left-player, $right-player) => string-join(" ")}" nicknames="{data($duel/player/@name) => string-join(" ")}" json="{$json}"/>
  return $new-node
for $gg in $ggs
return db:add("duelgg", $gg, "materialised-duels")
]]>
    </rest:text>
    </rest:query>
    ).map(_ => Unit)
  }

}