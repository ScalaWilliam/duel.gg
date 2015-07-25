declare option db:updindex 'true';

declare function local:within-time($a as xs:dateTime, $b as xs:dateTime, $max-diff as xs:dayTimeDuration) {
    let $max-diff := xs:dayTimeDuration("PT5S")
    return exists(
            for $diff in ($a - $b, $b - $a)
            where $diff ge xs:dayTimeDuration("PT0S") and $diff le $max-diff
            return $diff
    )
};
declare function local:parse-geo-ips($ips as xs:string) {
    let $map-parts :=
        for $a in tokenize($ips, ',')
        let $ip := substring-before($a, ' ')
        let $cc := substring-after($a, ' ')
        return map {$ip: $cc}
    return map:merge($map-parts)
};
declare function local:within($first as xs:dateTime, $second as xs:dateTime, $maxInterval as xs:dayTimeDuration) {
    let $zero := xs:dayTimeDuration("PT0S")
    let $smf := $second - $first
    let $fms := $first - $second
    return (
        ($smf ge $zero) and ($smf lt $maxInterval)
    ) or ( ($fms ge $zero ) and ($fms lt $maxInterval))
};
declare function local:get-random-id($chars as xs:string) {
    let $length := string-length($chars)
    let $new-chars :=
        for $i in 1 to $length
        let $idx := 1 + random:integer($length)
        return substring($chars, $idx, 1)
    return string-join($new-chars)
};
declare function local:duels-are-similar($a as node(), $b as node()) {
    (
        $a/@server eq $b/@server
    ) and (
        local:within(
                xs:dateTime($a/@start-time),
                xs:dateTime($b/@start-time),
                xs:dayTimeDuration("PT5M")
        )
    )
};

declare function local:ctfs-are-similar($a as node(), $b as node()) {
    (
        $a/@server eq $b/@server
    ) and (
        local:within(
                xs:dateTime($a/@start-time),
                xs:dateTime($b/@start-time),
                xs:dayTimeDuration("PT5M")
        )
    )
};

