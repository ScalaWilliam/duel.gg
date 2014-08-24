(:
    http://docs.marklogic.com/fn:format-dateTime
:)
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
declare variable $web-id as xs:string external;
for $duel in /duel[@web-id=$web-id]
let $dateTime := xs:dateTime($duel/@start-time)
return <duel>
    <title>{data($duel/players/player[1]/@name)} vs {data($duel/players/player[2]/@name)}</title>,
    <article class="duel single-duel-view">
        <header>
            <h3 class="mode-map">{data($duel/@mode)} @@ {data($duel/@map)}</h3>
            <h2 class="date-time">{local:display-time-2($dateTime)}</h2>
        </header>
        <section class="duel">
            <section class="score score-left">
                <p class="score">{data($duel/players/player[1]/@frags)}</p>
                <p class="name">{data($duel/players/player[1]/@name)}</p>
            </section>
            <section class="score score-right">
                <p class="score">{data($duel/players/player[2]/@frags)}</p>
                <p class="name">{data($duel/players/player[2]/@name)}</p>
            </section>
        </section>
    </article></duel>