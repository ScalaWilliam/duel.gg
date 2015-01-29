package plugins

import scala.async.Async.{async, await}
import play.api._
import plugins.DataSourcePlugin.UserProfile
import scala.concurrent.{Future, ExecutionContext}
import scala.xml.{PCData, Text}

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

  def getIndex = {
    val query = <rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
         let $duels := for $duel in /duel[@int-id]
          order by $duel/@start-time descending
          return
            <duel-card
              duelId="{$duel/@int-id}"
              leftPlayerScore="{($duel/player)[1]/@frags}" leftPlayerName="{($duel/player)[1]/@name}"
              rightPlayerScore="{($duel/player)[2]/@frags}" rightPlayerName="{($duel/player)[2]/@name}"
            mode="{data($duel/@mode)}" map="{data($duel/@map)}"><!-- --></duel-card>
          return <duels>{$duels[position() = 1 to 20]}</duels>
          ]]></rest:text>
    </rest:query>
    async {
      val r = await(BasexProviderPlugin.awaitPlugin.query(query))
      Option(r.body).filter(_.nonEmpty)
    }
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

  def getPlayerDuel(playerId: String, duelId: Int) = {
    async {
      val query = <rest:query xmlns:rest='http://basex.org/rest'>
        <rest:text><![CDATA[
          declare variable $duel-id as xs:string external;
          declare variable $user-id as xs:string external;
          for $ru in /registered-user[@id = $user-id]
          for $duel in /duel[@int-id = $duel-id]
          where $duel/player/@name = data($ru/nickname)
          let $older-duel :=
            (for $prev in /duel
            where $prev/player/@name = data($ru/nickname)
            where $prev/@start-time lt data($duel/@start-time)
            order by $prev/@start-time descending
            return $prev/@int-id)[1]
          let $newer-duel :=
            (for $prev in /duel
            where $prev/player/@name = data($ru/nickname)
            where $prev/@start-time gt data($duel/@start-time)
            order by $prev/@start-time ascending
            return $prev/@int-id)[1]
          let $left-player-id := data(/registered-user[nickname = data($duel/player[1]/@name)]/@id)
          let $right-player-id := data(/registered-user[nickname = data($duel/player[2]/@name)]/@id)
          let $has-demo:= exists(
            /downloaded-demo[@game-id  = data($duel/@simple-id)]
          )
          return <duel-detailed-card
          duelId="{$duel/@int-id}"
          atTime="{adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ())}"
          context="/player/{$user-id}/"
          leftPlayerScore="{data($duel/player[1]/@frags)}"
          leftPlayerName="{data($duel/player[1]/@name)}"
          map="{data($duel/@map)}"
          mode="{data($duel/@mode)}"
          rightPlayerScore="{data($duel/player[2]/@frags)}"
          rightPlayerName="{data($duel/player[2]/@name)}"
          >{
          if ($has-demo) then (attribute hasDemo {"true"}) else ()
          }{
          if ( empty($older-duel) ) then () else (attribute olderDuelId {data($older-duel)})
          }{
          if ( empty($newer-duel) ) then () else (attribute newerDuelId {data($newer-duel)})
          }{
          if ( empty($left-player-id) ) then () else (attribute leftPlayerId {data($left-player-id)})
          }{
          if ( empty($right-player-id) ) then () else (attribute rightPlayerId {data($right-player-id)})
          }<!-- --></duel-detailed-card>
          ]]></rest:text>
        <rest:variable name="duel-id" value={duelId.toString}/>
        <rest:variable name="user-id" value={playerId}/>
      </rest:query>
      val r = await(BasexProviderPlugin.awaitPlugin.query(query))
      Option(r.body).filter(_.nonEmpty)
    }

  }
  def getDuelDetailedCard(duelId: Int) = {
    async {
      val query = <rest:query xmlns:rest='http://basex.org/rest'>
        <rest:text><![CDATA[
          declare variable $duel-id as xs:string external;
          for $duel in /duel[@int-id = $duel-id]
          let $left-player-id :=data(
            (
            /registered-user[@game-nickname = data($duel/player[1]/@name)],
            for $ru in /registered-user
            for $nick in $ru/nickname
            where $nick = data($duel/player[1]/@name)
            where $nick/@from < $duel/@start-time
            where $nick/@to > $duel/@start-time
            return $ru
            )[1]/@id)
          let $older-duel :=
            (for $prev in /duel
            where $prev/@start-time lt data($duel/@start-time)
            order by $prev/@start-time descending
            return $prev/@int-id)[1]
          let $newer-duel :=
            (for $prev in /duel
            where $prev/@start-time gt data($duel/@start-time)
            order by $prev/@start-time ascending
            return $prev/@int-id)[1]
          let $right-player-id :=data(
            (
            /registered-user[@game-nickname = data($duel/player[2]/@name)],
            for $ru in /registered-user
            for $nick in $ru/nickname
            where $nick = data($duel/player[2]/@name)
            where $nick/@from < $duel/@start-time
            where $nick/@to > $duel/@start-time
            return $ru
            )[1]/@id)
          let $has-demo:= exists(
            /downloaded-demo[@game-id  = data($duel/@simple-id)]
          )
          return <duel-detailed-card
          duelId="{$duel/@int-id}"
          atTime="{adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ())}"
          leftPlayerScore="{data($duel/player[1]/@frags)}"
           map="{data($duel/@map)}"
          mode="{data($duel/@mode)}"
          leftPlayerName="{data($duel/player[1]/@name)}"
          rightPlayerScore="{data($duel/player[2]/@frags)}"
          rightPlayerName="{data($duel/player[2]/@name)}"
          >{
          if ($has-demo) then (attribute hasDemo {"true"}) else ()
          }{
          if ( empty($older-duel) ) then () else (attribute olderDuelId {data($older-duel)})
          }{
          if ( empty($newer-duel) ) then () else (attribute newerDuelId {data($newer-duel)})
          }{

          if ( empty($left-player-id) ) then () else (attribute leftPlayerId {data($left-player-id)})
          }{
          if ( empty($right-player-id) ) then () else (attribute rightPlayerId {data($right-player-id)})
          }<!-- --></duel-detailed-card>
          ]]></rest:text><rest:variable name="duel-id" value={duelId.toString}/></rest:query>
      val r = await(BasexProviderPlugin.awaitPlugin.query(query))
      Option(r.body).filter(_.nonEmpty)
    }

  }

  def getNickname(nickname: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val xml = <rest:query xmlns:rest="http://basex.org/rest">
      <rest:text><![CDATA[
declare variable $nickname as xs:string external;
for $nickname in $nickname
let $duels :=
  for $duel in /duel
  where $duel/player/@name = $nickname
  return $duel
where not(empty($duels))
return <player-card name="{$nickname}">
{
for $duel in $duels
order by $duel/@start-time descending
return
  <duel-card
  atTime="{adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ())}"
    duelId="{$duel/@int-id}"
    leftPlayerScore="{($duel/player)[1]/@frags}" leftPlayerName="{($duel/player)[1]/@name}"
    rightPlayerScore="{($duel/player)[2]/@frags}" rightPlayerName="{($duel/player)[2]/@name}"
    mode="{$duel/@mode}" map="{$duel/@map}"><!-- --></duel-card>
}
</player-card>
]]>
      </rest:text>
      <rest:variable name="nickname" value={nickname}/>
    </rest:query>
    BasexProviderPlugin.awaitPlugin.query(xml).map(Option(_).filter(_.body.nonEmpty).map(_.body))
  }

  def getUsername(userId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val xml = <rest:query xmlns:rest="http://basex.org/rest">
      <rest:text><![CDATA[
declare variable $user-id as xs:string external;
for $ru in /registered-user[@id = $user-id]
let $current-nickname := $ru/nickname[not(@to)]
let $past-nicknames := $ru/nickname[@to]
return <player-card name="{$ru/@game-nickname}">
{
let $duels := (
  for $duel in /duel
  where $duel/@start-time ge $current-nickname/@from
  where $duel/player/@name = data($current-nickname)
  return $duel,
  for $nickname in $past-nicknames
  for $duel in /duel
  where $duel/player/@name = data($current-nickname)
  where $duel/@start-time ge $nickname/@from
  where $duel/@start-time le $nickname/@to
  return $duel
)
for $duel in $duels
let $start-time := $duel/@start-time
order by $start-time descending
return
  <duel-card
    atTime="{adjust-dateTime-to-timezone(xs:dateTime($duel/@start-time), ())}"
    duelId="{$duel/@int-id}"
    context="/player/{$user-id}/"
    leftPlayerScore="{($duel/player)[1]/@frags}" leftPlayerName="{($duel/player)[1]/@name}"
    rightPlayerScore="{($duel/player)[2]/@frags}" rightPlayerName="{($duel/player)[2]/@name}"
    mode="{$duel/@mode}" map="{$duel/@map}"><!-- --></duel-card>
}
</player-card>
]]>
      </rest:text>
      <rest:variable name="user-id" value={userId}/>
    </rest:query>
    BasexProviderPlugin.awaitPlugin.query(xml).map(Option(_).filter(_.body.nonEmpty).map(_.body))
  }

  def getPlayerCounts(username: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    BasexProviderPlugin.awaitPlugin.queryOption(<rest:query xmlns:rest='http://basex.org/rest'>
      <rest:text><![CDATA[
      declare variable $user-id as xs:string external;
    let $ru := /registered-user[@id = $user-id]
    let $player-duels :=
    for $duel in /duel
      where $duel/player[@name = data($ru/nickname)]
    return $duel
    let $dates :=
    for $dayN in 0 to 90
    let $day := xs:date(current-date() - (xs:dayTimeDuration('P1D') * $dayN))
    let $day-counts :=
    for $duel in $player-duels
    where xs:date(adjust-dateTime-to-timezone(xs:dateTime(data($duel/@start-time)),())) = $day
    return $duel
    let $cnt := count($day-counts)
    let $weight := if ( $cnt = 0 ) then ("0") else if ( $cnt lt 5 ) then ("1") else if ($cnt lt 10) then ("2") else ("3")
    return <date weight="{$weight}" count="{$cnt}" title="{$day}: {$cnt}"><!----></date>
    return <dates>{$dates}</dates>

    ]]></rest:text>
      <rest:variable name="user-id" value={username}/>
    </rest:query>)
  }
}
object DataSourcePlugin {

  case class UserProfile(name: String, profileData: String)
  def plugin: DataSourcePlugin = Play.current.plugin[DataSourcePlugin]
    .getOrElse(throw new RuntimeException("DataSourcePlugin plugin not loaded"))
}