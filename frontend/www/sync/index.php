<?php
if ( !isset($_SERVER['SYNC_KEY']) ) die("no SYNC_KEY set");
if ( $_GET['sync-key'] !== $_SERVER['SYNC_KEY'] ) die("provided sync key is invalid");

system("git checkout master");
system("git pull");
