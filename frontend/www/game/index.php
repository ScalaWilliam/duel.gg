<!doctype html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>Homepage</title>
    <link href='http://fonts.googleapis.com/css?family=Permanent+Marker|Signika:400,700' rel='stylesheet' type='text/css'>
    <link href='http://duel.gg/assets/stylesheets/main.css' rel="stylesheet" type="text/css"/>
    <meta name="description" content="duel.gg is a match league for the Sauerbraten multiplayer first-person shooter">
    <meta name="keywords" content="online FPS, first person shooter, sauerbraten league, sauerbraten, sauerbraten duel">
    <link href='http://duel.gg/news/atom/' rel="alternate" type="application/atom+xml" title="Atom feed of news articles"/>
</head>
<body>

<div role="main">
    <header id="top">
        <h1><a href="/">Duel? GG!</a></h1>
        <h2>The <a href="http://www.sauerbraten.org/">Sauerbraten</a> Game League</h2>
    </header>

    <div id="menu">
        <a href="/">Home
            <paper-button class="colored custom" tabindex="1">
                <core-icon icon="home" style="width:2em;height:2em"></core-icon>
                Home
            </paper-button>
        </a>
        <a href="/players/">
            <paper-button class="colored custom" tabindex="0">
                <core-icon icon="social:people" style="width:2em;height:2em"></core-icon>
                Players
            </paper-button>
        </a>
        <a href="/search/">
            <paper-button class="colored custom" tabindex="0" aria-label="search" id="search-btn">
                <core-icon icon="search" style="width:2em;height:2em"></core-icon>
                Search
            </paper-button>
        </a>
        <a href="/servers/">
            <paper-button class="colored custom" tabindex="2">
                <core-icon icon="hardware:gamepad" style="width:2em;height:2em"></core-icon>
                Servers
            </paper-button>
        </a>
        <a href="/questions/">
            <paper-button class="colored custom" tabindex="3">
                <core-icon icon="help" style="width:2em;height:2em"></core-icon>
                Questions
            </paper-button>
        </a>

        <a href="/login/">
            <paper-button class="colored custom" tabindex="4">
                <core-icon icon="open-in-browser" style="width:2em;height:2em"></core-icon>
                Register with Google
            </paper-button>
        </a>



        <a href="/news/atom/" title="Atom News Feed">
            <paper-button class="colored custom" tabindex="1">
                <core-icon src="/assets/appbar.rss.svg" style="width: 2em; height: 2em;"></core-icon>
            </paper-button>
        </a>
        <br/>

    </div>
    <div id="main">

        <div id="download-ac-button">
            <a href="http://sourceforge.net/projects/sauerbraten/files/sauerbraten/2013_01_04/sauerbraten_2013_02_03_collect_edition_windows.exe/download" target="_blank">
                <paper-button class="colored custom">

                    <core-icon icon="file-download" style="width:2em;height:2em"></core-icon>
                    Download the Sauerbraten Collect Edition first-person shooter (576MB, Windows .exe)

                </paper-button>
            </a>
        </div>

        <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

            <?php
            $game = json_decode(file_get_contents("http://alfa.duel.gg/api/game/".rawurlencode($_GET['id'])."/"), true);
            require_once("../render_game.php");
            render_duel($game);
            ?>

        </div>
    </div>
</div>
</body>
</html>