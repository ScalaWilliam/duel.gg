<?php
function basex_query($query_xml)
{
    $url = 'http://odin.duel.gg:3238/rest/duelgg/';
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_USERPWD, "admin:admin");
    curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $query_xml);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return $response;
}