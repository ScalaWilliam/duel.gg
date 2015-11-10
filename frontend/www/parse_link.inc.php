<?php
function get_links() {
    global $http_response_header;
    $lnk = "Link: ";
    foreach($http_response_header as $h) {
        if ( substr($h, 0, strlen($lnk) ) === $lnk) {
            $lnkv = substr($h, strlen($lnk));
            return parse_link($lnkv);
        }
    }
    return [];
}
function parse_link($link) {
    $ret = [];
    foreach(explode(", ", $link) as $a_link) {
        $b_link = new stdClass();
        $parts = explode("; ", $a_link);
        $path = array_shift($parts);
        if ( substr($path, 0, 1) == '<' ) {
            $path = substr($path, 1, strlen($path) - 2);
        }
        $b_link->path = $path;
        foreach($parts as $part) {
            list($key, $value) = explode("=", $part);
            if ( substr($value, 0, 1) == "\"" ) {
                $value = substr($value, 1, strlen($value) -2);
            }
            $b_link->{$key} = $value;
        }
        $ret[] = $b_link;
    }
    return $ret;
}
//$link = '</games/before/2014-10-04T06:43:32Z/?type=duel&limit=5>; rel="previous"; title="Previous games", </games/live/?type=duel>; rel="related"; title="Live game updates SSE stream", </games/new/?type=duel>; rel="related"; title="New games SSE stream"';
//header("Content-Type: text/plain");
//var_dump(parse_link($link));