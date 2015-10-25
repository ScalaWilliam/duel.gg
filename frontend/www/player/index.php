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
        <ol>
            <li><a href="/">Home</a></li>
            <li><a href="/questions/">Questions</a></li>
            <li><a href="/news/">News</a></li>
        </ol>
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

            <div id="first">
                <style scoped>
                    #news header {
                        display:flex;
                        margin-bottom:0.6em;
                    }
                    #news h2 {
                        margin-top:0;
                        margin-bottom: 0;
                    }
                    #news time {
                        flex:1;
                        align-self:flex-end;
                        margin-bottom:0.15em;
                        margin-left:0.6em;
                    }
                </style>
                <br/>

                <div id="dduelss">
                    <pre><?php
    $url = "http://alfa.duel.gg/api/duels/recent/?player=".rawurlencode($_GET['name']);
                        if ( isset($_GET['to'])) {
                            $url = "http://alfa.duel.gg/api/duels/to/".rawurlencode($_GET['to'])."/?player=" . rawurlencode($_GET['name']);
                        }
                        $recentduels = json_decode(file_get_contents($url), true);

                        ?></pre>
                </div>

                <?php
                require_once("../render_game.php");
                foreach($recentduels as $duel) {
                    render_duel($duel);
                }

                ?>
                <?php if ( isset($duel)) {
?>
                <p><a href="?name=<?php echo rawurlencode($_GET['name']); ?>&amp;to=<?php echo rawurlencode($duel['startTimeText']); ?>">Earlier games</a></p>
<?php } ?>
                <div id="rest">

                </div>

            </div>
        </div>
    </div>
</body>
</html>
