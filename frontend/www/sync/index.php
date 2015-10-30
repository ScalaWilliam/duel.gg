<?php
if ( !isset($_SERVER['SYNC_KEY']) ) die("no SYNC_KEY set");
if ( $_GET['sync-key'] !== $_SERVER['SYNC_KEY'] ) die("provided sync key is invalid");

if ( $_SERVER['HTTP_POST'] == "alfa.duel.gg" ) {
    $post_body = file_get_contents('php://input');
    $json = json_decode($post_body, true);
    if ( strpos($post_body, "frontend") !== false ) {
        $sha = $json['after'];
        if (ctype_alnum($sha)) {
            system("git fetch");
            system("git checkout $sha");
        }
    }
} else {
    system("git checkout master");
    system("git pull");
}
