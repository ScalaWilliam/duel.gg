let $bdy := <query xmlns="http://basex.org/rest"><text>{'
<duels>{
for $duel in db:open("db-stage")/duel
where xs:date(xs:dateTime($duel/@start-time)) ge xs:date("2015-01-20")
where xs:date(xs:dateTime($duel/@start-time)) le xs:date("2015-01-29")
order by $duel/@start-time ascending
return $duel
}</duels>
'}</text></query>
let $request := <http:request href='http://odin.duel.gg:8984/rest' method='post' username='admin' password='admin' send-authorization='true'>
<http:header name="Accept" value="text/xml"/>
<http:body media-type='application/xml'>{$bdy}</http:body>
</http:request>
for $duel in http:send-request($request)//duel
where every $ip in $duel//@ip satisfies (exists(/duel//@partial-ip = data($ip)))
let $int-id := xs:int(substring(string(abs(convert:integer-from-base(string(xs:hexBinary(hash:sha256(string($duel/@simple-id)))), 16))), 1, 9))
let $new-version :=
  copy $duel := $duel
  modify (
    insert node (attribute int-id {$int-id}) as first into $duel,
    for $player in $duel//player
    let $new-player :=
      copy $player := $player
      modify (
        delete node $player/@ip,
        insert node (attribute partial-ip {data($player/@ip)}) into $player,
        insert node (/duel//player[@partial-ip = $player/@ip])[1]/@country-code into $player
      )
      return $player
    return insert node ($new-player) as first into $duel
    ,
    delete node $duel/players
  )
  return $duel
return db:add("duelgg", $new-version, "reimported-missing-duels")