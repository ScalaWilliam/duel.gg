declare option db:updindex 'true';

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
declare function local:get-new-duel-id($duels as node()*, $chars as xs:string) {
    let $new-id := local:get-random-id($chars)
    return
        if (empty($duels[@web-id = $new-id]))
        then ($new-id)
        else (local:get-new-duel-id($duels, $chars))
};
declare function local:get-new-ctf-id($ctfs as node()*, $chars as xs:string) {
    let $new-id := local:get-random-id($chars)
    return
        if (empty($ctfs[@web-id = $new-id]))
        then ($new-id)
        else (local:get-new-ctf-id($ctfs, $chars))
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

declare %updating function local:add-new-duel($new-duel-id as xs:string, $db-name as xs:string, $new-duel as node()) {
    let $updated-duel :=
        copy $updated-duel := $new-duel
        modify (
            rename node $updated-duel as 'duel',
            insert node (attribute {'web-id'} { $new-duel-id }) into $updated-duel
        )
        return $updated-duel
    let $meta-data-id := data($updated-duel/@meta-id)
    return (db:add($db-name, $updated-duel, $meta-data-id))
};

declare %updating function local:add-new-ctf($new-ctf-id as xs:string, $db-name as xs:string, $new-ctf as node()) {
    let $updated-ctf :=
        copy $updated-ctf := $new-ctf
        modify (
            rename node $updated-ctf as 'ctf',
            insert node (attribute {'web-id'} { $new-ctf-id }) into $updated-ctf
        )
        return $updated-ctf
    let $meta-data-id := data($updated-ctf/@meta-id)
    return (db:add($db-name, $updated-ctf, $meta-data-id))
};
