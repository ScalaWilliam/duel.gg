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
let $a := $duel/players/player[1]
let $a-url := concat("/?player=", data($a/@name))
let $b := $duel/players/player[2]
let $b-url := concat("/?player=", data($b/@name))
return <duel>
    <title>{data($a/@name)} vs {data($b/@name)}</title>,
    <article class="duel single-duel-view">
        <header>
            <h3 class="mode-map">{data($duel/@mode)} @ {data($duel/@map)}</h3>
            <h2 class="date-time">{local:display-time-2($dateTime)}</h2>
        </header>
        <section class="duel">
            <section class="score score-left">
                <p class="score">{data($a/@frags)}</p>
                <p class="name"><a href="{$a-url}">{data($a/@name)}</a></p>
            </section>
            <section class="score score-right">
                <p class="score">{data($b/@frags)}</p>
                <p class="name"><a href="{$b-url}">{data($b/@name)}</a></p>
            </section>
            <p class="server-detail">Server:
                {
                    data(subsequence((/server[@server = $duel/@server]/@alias, $duel/@server), 1, 1))
                }
            </p>
        </section>
    </article></duel>