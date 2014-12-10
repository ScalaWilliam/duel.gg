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
let $duel-chart :=
let $max := max(for $frag in data($duel//frags) return xs:int($frag))
let $step-size := ceiling($max div 6.0)
let $texts := for $i in (0 to 6) return $i * $step-size
let $max-chart-score := $step-size * 6
let $by := 127.36218
let $ty := 10.36218
let $rx := 287
let $lx := 84
let $duration := data($duel/@duration)
let $player-gs :=
  for $player in $duel/players/player
  let $is-first := $player is ($duel/players/player)[1]
  let $items :=
    for $frag in $player/frags
    order by xs:integer(data($frag/@at)) ascending
    let $t := data($frag/@at)
    let $x := $lx + (($rx - $lx) div $duration * $t)
    let $score := data($frag)
    let $y := $by + (($score div $max-chart-score) * ($ty - $by))
    return "L " || $x ||"," || $y
  return "M " || $lx || "," || $by || " " || string-join($items, " ")
return
<svg
   xmlns:svg="http://www.w3.org/2000/svg"
   xmlns="http://www.w3.org/2000/svg"
   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
   width="240"
   height="139"
   id="svg2"
   version="1.1"
   inkscape:version="0.48.4 r9939"
   sodipodi:docname="history-sample.svg">
  <defs
     id="defs4" />
  <g
     inkscape:label="Layer 1"
     inkscape:groupmode="layer"
     id="layer1"
     transform="translate(3,1.6378174)">
    <rect
       style="fill:#d8f0f0;fill-opacity:1;stroke:none"
       id="rect2987"
       width="137"
       height="81"
       x="-297"
       y="158.36218" />
    <rect
       style="fill:#303048;fill-opacity:1;stroke:none"
       id="rect4492"
       width="240"
       height="140"
       x="-1"
       y="-1.6378174" />
    <g
       id="lines"
       transform="translate(-60,0)">
      <path
         sodipodi:nodetypes="cccc"
         inkscape:connector-curvature="0"
         id="path3757"
         d="{$player-gs[1]}"
         style="fill:none;stroke:#d8f0f0;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" />
      <path
         sodipodi:nodetypes="cccc"
         style="fill:none;stroke:#f0d8d9;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none;stroke-dashoffset:0"
         d="{$player-gs[2]}"
         id="path3759"
         inkscape:connector-curvature="0" />
    </g>
    <g
       id="background">
      <path
         inkscape:connector-curvature="0"
         id="path3761"
         d="m 23,109.36218 207,0"
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0" />
      <path
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0"
         d="m 23,127.36218 207,0"
         id="path3763"
         inkscape:connector-curvature="0" />
      <path
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0"
         d="m 23,89.36218 207,0"
         id="path3765"
         inkscape:connector-curvature="0" />
      <path
         inkscape:connector-curvature="0"
         id="path3767"
         d="m 23,69.36218 207,0"
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0" />
      <path
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0"
         d="m 23,49.36218 207,0"
         id="path3769"
         inkscape:connector-curvature="0" />
      <path
         inkscape:connector-curvature="0"
         id="path3771"
         d="m 23,29.36218 207,0"
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0" />
      <path
         style="fill:#d8f0f0;fill-opacity:1;stroke:#fcfbe3;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:1, 3;stroke-dashoffset:0"
         d="m 23,9.36218 207,0"
         id="path3773"
         inkscape:connector-curvature="0" />
      <text
         sodipodi:linespacing="160%"
         id="text3775"
         y="14.362183"
         x="17.171875"
         style="font-size:40px;font-style:normal;font-weight:normal;line-height:160.00000238%;letter-spacing:0px;word-spacing:0px;fill:#fcfbe3;fill-opacity:1;stroke:none;font-family:Sans"
         xml:space="preserve"><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           y="14.362183"
           x="17.171875"
           id="tspan3777"
           sodipodi:role="line">{$texts[7]}</tspan><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3789"
           y="33.562183"
           x="17.171875"
           sodipodi:role="line">{$texts[6]}</tspan><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3787"
           y="52.762184"
           x="17.171875"
           sodipodi:role="line">{$texts[5]}</tspan><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3785"
           y="71.962181"
           x="17.171875"
           sodipodi:role="line">{$texts[4]}</tspan><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3783"
           y="91.162186"
           x="17.171875"
           sodipodi:role="line">{$texts[3]}</tspan><tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3781"
           y="110.36218"
           x="17.171875"
           sodipodi:role="line">{$texts[2]}</tspan>
                      <tspan xmlns="http://www.w3.org/2000/svg" xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" sodipodi:role="line" x="17.171875" y="129.56218" id="tspan3779" style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans">0</tspan>
<tspan
           style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:160.00000238%;text-anchor:end;fill:#fcfbe3;fill-opacity:1;font-family:Open Sans;-inkscape-font-specification:Open Sans"
           id="tspan3779"
           y="129.56218"
           x="17.171875"
           sodipodi:role="line">-</tspan></text>
    </g>
    <text
       xml:space="preserve"
       style="font-size:40px;font-style:normal;font-weight:normal;line-height:125%;letter-spacing:0px;word-spacing:0px;fill:#000000;fill-opacity:1;stroke:none;font-family:Sans"
       x="154"
       y="216.36218"
       id="text3791"
       sodipodi:linespacing="125%"><tspan
         sodipodi:role="line"
         id="tspan3793"
         x="154"
         y="216.36218" /></text>
  </g>
</svg>
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
            {$duel-chart}
        </section>
    </article></duel>