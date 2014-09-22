(:declare variable $servers-list as xs:string external;:)
(:let $servers := tokenize($servers-list, ","):)
let $recorded-duels :=
    for $duel in db:open("db-stage")/duel
    (:where some $server in $servers satisfies starts-with(data($duel/@server), $server):)
    return $duel
let $total-games := count($recorded-duels)
let $stats-list := <stats total-games="{$total-games}">
    {
        let $players :=
            for $name in distinct-values($recorded-duels/@winner)
            let $player-games := count($recorded-duels/players/player[@name = $name])
            where $player-games ge 5
            let $player-wins := count($recorded-duels[@winner = $name])
            let $score := 45 * math:log($player-games) + $player-wins * $player-wins div $player-games
            order by $score descending, $player-games descending
            return <player wins="{$player-wins}" score="{xs:integer($score)}" games="{$player-games}" name="{$name}"/>
        for $position in $players/position()
        return
            copy $player-c := $players[$position]
            modify ( insert node ( attribute { "rank" } { $position }) into $player-c )
            return $player-c
    }
</stats>
return <table><thead><tr><th>#</th><th>Player</th><th>Games</th><th>Wins</th><th>Score</th></tr></thead>
<tbody>{
for $player in $stats-list/player
return <tr>
  <th>{data($player/@rank)}</th>
  <th><a href="{concat('/?player=', data($player/@name))}">{data($player/@name)}</a></th>
  <td>{data($player/@games)}</td>
  <td>{data($player/@wins)}</td>
  <td>{data($player/@score)}</td>
</tr>}</tbody>
</table>