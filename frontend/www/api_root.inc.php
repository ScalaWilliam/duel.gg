<?php
$api_root = getenv("API_ROOT") ?: $_ENV['API_ROOT'] ?: $_SERVER['API_ROOT'] ?: 'http://api.duel.gg';
$host = $api_root;