declare function local:display-time($dateTime as xs:dateTime) {
    let $now := fn:current-dateTime()
    let $formatted := fn:format-dateTime($dateTime, "[Y01]/[M01]/[D01]")
    let $day-name := fn:format-dateTime($dateTime, "[FNn]")
    let $hour-ago := fn:current-dateTime() - xs:dayTimeDuration("PT1H")
    let $week-ago := fn:current-dateTime() - xs:dayTimeDuration("P7D")
    let $display :=
        if ( ($dateTime le $now) and ($dateTime ge $hour-ago))
        then ("Just now")
        else if ( ($dateTime le $now) and ($dateTime ge $week-ago))
        then (concat("On ",$day-name))
        else ($formatted)
    return $display
};
declare function local:index-duel($duel as element()) {
    <li class="duel-item"><a href="{data($duel/@web-id)}">
        <header>
            <h2>{data($duel/players/player[1]/@name)} vs {data($duel/players/player[2]/@name)}</h2>
            <h3>{data($duel/@mode)} @ {data($duel/@map)}</h3>
        </header>
        <footer>
            <p class="score">{data($duel/players/player[1]/@frags)}-{data($duel/players/player[2]/@frags)}</p>
        </footer></a>
    </li>
};
declare function local:display-time-2($dateTime as xs:dateTime) {
    let $now := fn:current-dateTime()
    let $formatted := fn:format-dateTime($dateTime, "[Y01]/[M01]/[D01]")
    let $day-name := fn:format-dateTime($dateTime, "[FNn], [D] [MNn]")
    let $hour-ago :=fn:current-dateTime() - xs:dayTimeDuration("PT1H")
    let $week-ago := fn:current-dateTime() - xs:dayTimeDuration("P7D")
    let $display :=
        if ( ($dateTime le $now) and ($dateTime ge $hour-ago) )
        then ("Just now")
        else if ( ($dateTime le $now) and ($dateTime ge $week-ago) )
        then ($day-name)
        else ($formatted)
    return $display
};
(:let $until := xs:dateTime(fn:current-dateTime() - xs:dayTimeDuration("P10D")):)
let $duels-items :=
    for $duel in /duel[@web-id]
    let $dateTime := xs:dateTime($duel/@start-time)
    (:where $dateTime ge $until:)
    order by $dateTime descending
    let $readableTime := local:display-time-2($dateTime)
    group by $readableTime
    return
        <li class="duels-day"><h3>{$readableTime}</h3>
        <ol class="duels-day-items">{
            for $duel in $duel
            return local:index-duel($duel)
        }</ol></li>
return <ol class="duels-days-list"><li class="duels-day"><h3>Just now</h3><ol class="duels-day-items"><li class="duel-item">
    <a href="nddssaa" >
        <header>
            <h2>!s]Gangler vs |&gt;BM&lt;|Partizan</h2>
            <h3>instagib @ turbine</h3>
        </header>
        <footer>
            <p class="score">118-79</p>
        </footer>
    </a>
</li></ol></li>{fn:subsequence($duels-items, 1, 4)}</ol>