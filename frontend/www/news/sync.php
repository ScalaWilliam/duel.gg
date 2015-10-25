<?php

if ( !isset($_SERVER['SYNC_KEY']) ) die("no SYNC_KEY set");
if ( $_GET['sync-key'] !== $_SERVER['SYNC_KEY'] ) die("provided sync key is invalid");

require("../basex.inc.php");

file_put_contents("tumblr.rss.xml", file_get_contents("http://duelgg.tumblr.com/rss"));
file_put_contents("tumblr.api-read.xml", file_get_contents("http://duelgg.tumblr.com/api/read"));

header("Content-Type: application/xml");
?>
<XML>
    <RSS><?php echo file_get_contents("tumblr.rss.xml"); ?></RSS>
    <FEED><?php echo file_get_contents("tumblr.api-read.xml"); ?></FEED>
</XML>