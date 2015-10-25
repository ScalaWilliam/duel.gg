<?php

//if ( !isset($_SERVER['SYNC_KEY']) ) die("no SYNC_KEY set");
//if ( $_GET['sync-key'] !== $_SERVER['SYNC_KEY'] ) die("provided sync key is invalid");

require("../basex.inc.php");
$simplexml = simplexml_load_string('<rest:query xmlns:rest="http://basex.org/rest"><rest:text></rest:text></rest:query>');
$simplexml->text = file_get_contents("basex_articles.xq");

file_put_contents("basex_articles.xml", basex_query($simplexml->asXML()));
file_put_contents("tumblr.rss.xml", file_get_contents("http://duelgg.tumblr.com/rss"));
file_put_contents("tumblr.api-read.xml", file_get_contents("http://duelgg.tumblr.com/api/read"));

header("Content-Type: application/xml");
$dom = dom_import_simplexml(simplexml_load_string("<XML/>"));
$dom->appendChild($dom->ownerDocument->importNode(dom_import_simplexml(simplexml_load_file("tumblr.rss.xml")), true));
$dom->appendChild($dom->ownerDocument->importNode(dom_import_simplexml(simplexml_load_file("tumblr.api-read.xml")), true));
$dom->appendChild($dom->ownerDocument->importNode(dom_import_simplexml(simplexml_load_file("basex_articles.xml")), true));
echo $dom->ownerDocument->saveXML();
