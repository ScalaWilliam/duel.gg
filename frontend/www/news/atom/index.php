<?php
$url = 'http://odin.duel.gg:3238/rest/duelgg/';
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_USERPWD, "admin:admin");
curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
curl_setopt($ch, CURLOPT_POST, 1);
$atomxq = file_get_contents("atom.xq");
$template = '<rest:query xmlns:rest="http://basex.org/rest"><rest:text></rest:text></rest:query>';
$simplexml = simplexml_load_string($template);
$simplexml->text = $atomxq;
$xml = $simplexml->asXML();
curl_setopt($ch, CURLOPT_POSTFIELDS, $xml);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);
header("Content-Type: application/atom+xml");
echo $response;
