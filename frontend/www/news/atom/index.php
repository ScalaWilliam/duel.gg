<?php
require("../../basex.inc.php");
$atomxq = file_get_contents("atom.xq");
$template = '<rest:query xmlns:rest="http://basex.org/rest"><rest:text></rest:text></rest:query>';
$simplexml = simplexml_load_string($template);
$simplexml->text = $atomxq;
$xml = $simplexml->asXML();
header("Content-Type: application/atom+xml");
echo basex_query($xml);
