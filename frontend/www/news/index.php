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




            <article id="questions">
                <style scoped>
                    h2:target {

                        -webkit-animation: highlighter 5s 2; /* Safari 4+ */
                        -moz-animation:    highlighter 5s 2; /* Fx 5+ */
                        -o-animation:      highlighter 5s 2; /* Opera 12+ */
                        animation:         highlighter 5s 2; /* IE 10+, Fx 29+ */
                    }
                    @keyframes highlighter {
                        0%   { text-shadow:1px 1px 1px gold, -1px 1px 1px gold, 1px -1px 1px gold, -1px -1px 1px gold; }
                        100% { text-shadow:0 0 2px white; }
                    }

                    @-moz-keyframes highlighter {
                        0%   { text-shadow:1px 1px 1px gold, -1px 1px 1px gold, 1px -1px 1px gold, -1px -1px 1px gold; }
                        100% { text-shadow:0 0 2px white; }
                    }

                    @-webkit-keyframes highlighter {
                        0%   { text-shadow:1px 1px 1px gold, -1px 1px 1px gold, 1px -1px 1px gold, -1px -1px 1px gold; }
                        100% { text-shadow:0 0 2px white; }
                    }

                </style>

                <h2>What is duel.gg?</h2>
                <p>duel.gg collects duel results from selected <a href="http://www.sauerbraten.org/" target="_blank">Sauerbraten</a> servers.</p>
                <p>Here's some Sauerbraten:</p>
                <iframe width="560" height="315" src="//www.youtube-nocookie.com/embed/fX7o-1OH-WM?rel=0" frameborder="0" allowfullscreen></iframe>

                <h2>What's the magic behind duel.gg?</h2>
                <p>The genius of <a target="_blank" href="https://www.scalawilliam.com/">William Narmontas, professional software engineer</a>.<br/>Technology-wise, it's Scala, Akka, Hazelcast, BaseX, XQuery and Play!</p>

                <h2>Where's ELO? Is there a ladder?</h2>
                <p>There will be no ELO. We have other priorities.</p>

                <h2>I have a suggestion</h2>
                <p>Go to IRC and let us now &mdash; <a href="https://webchat.gamesurge.net/?channels=duel.gg">#duel.gg @@ GameSurge</a> (nickname Drakas).</p>

                <h2>The site is slow...</h2>
                <p>Google Chrome is the recommended browser because I used bleeding edge technology.</p>

                <h2>Something is wrong...</h2>
                <p>duel.gg is still in beta.</p>

                <h2 id="several-nicknames"><a href="#several-nicknames">I have used several nicknames before 1st February 2015</a></h2>
                <p>We'll aggregate each nickname of yours that has at least 10 duels. Send Drakas a message on IRC.</p>
                <p>From 1st February 2015 only a single nickname for a user is allowed at any time.</p>
                <p>If you want to change your nickname, you must let me know.</p>

                <h2>Why the Google sign in?</h2>
                <p>To save me time. These things don't come free, you know.</p>

                <h2>Why should I register?</h2>
                <p>Get a complete profile, we're not done yet, you know.</p>

                <h2 id="my-ip-not-found"><a href="#my-ip-not-found">My IP is not found? I can't register</a></h2>
                <p>Play a duel right now. Your IP will get registered and you'll be able to register with the nickname that you used.</p>
                <p>We verify your IP only once.</p>

                <h2 id="who-else-helped"><a href="#who-else-helped">Who else helped with this project?</a></h2>
                <p><a href="/player/swatllama/">swatllama</a>, greenadiss and <a href="/player/frosty/">Frosty</a> helped with ideas and testing.</p>


                <h2 id="demos"><a href="#demos">Do you collect demos too?</a></h2>
                <p>We collect demos from PSL and effic.me servers. If you would like to share your server demos, drop me a link.
                    They will be available eventually.
                </p>



        </div>
    </div>
</div>
</body>
</html>