<?php

if ( !isset($_ENV['SYNC_KEY']) ) die("no SYNC_KEY set");
if ( $_GET['sync-key'] !== $_ENV['SYNC_KEY'] ) die("provided sync key is invalid");

require("../basex.inc.php");

file_put_contents("tumblr.rss.xml", file_get_contents("http://duelgg.tumblr.com/rss"));
file_put_contents("tumblr.api-read.xml", file_get_contents("http://duelgg.tumblr.com/api/read"));

$read = simplexml_load_file("tumblr.api-read.xml");
foreach($read->posts->post as $post) {
    if ( isset($post['url-with-slug']) && preg_match('/.*\/post\/[0-9]+\/([a-zA-Z0-9-]+)$/', $post['url-with-slug'], $matches)
    && isset($post['url']) && preg_match('/^http:\/\/duelgg.tumblr.com\/post\/[0-9]+$/', $post['url'], $_) ) {
        $f = $matches[1];
        @mkdir($f);
        @file_put_contents($f . "/index.php", "<" . "?php \$tumblr_item = '".$post['url']."'; require('../view_item.php');");
        if ( strpos($gitignore_contents, $f) === false ) {
            $gitignore_contents .= "\n$f/\n";
        }
    }
}
