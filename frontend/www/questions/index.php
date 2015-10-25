<?php require("../render.php"); ?>
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

</article>

    <?php echo $foot; ?>
