<?php require("../render.inc.php"); ?>
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
    <p>duel.gg is a
        <a href="http://www.sauerbraten.org/" target="_blank">Sauerbraten</a>
        public server game aggregation platform.</p>
    <p>Here's some Sauerbraten:</p>
    <iframe width="560" height="315" src="//www.youtube-nocookie.com/embed/fX7o-1OH-WM?rel=0" frameborder="0" allowfullscreen></iframe>

    <h2>What's the magic behind duel.gg?</h2>
    <p>The genius of <a target="_blank" href="https://www.scalawilliam.com/">William Narmontas, professional software engineer</a>.<br/>
        Technology-wise, it's Scala, Play! Akka, BaseX, ...
    </p>

    <h2>Is duel.gg open source?</h2>
    <p>Yes. See: <a target="_blank" href="https://github.com/scalawilliam/duel.gg">duel.gg repository on GitHub</a>.</p>

    <h2>I have a suggestion or something is wrong</h2>
    <p>Make a <a target="_blank" href="https://github.com/ScalaWilliam/duel.gg/issues">GitHub issue</a>.</p>

    <h2>What are your plans?</h2>
    <p>To get people building on top of this project. Previously it was closed source but I realised there's no huge benefit to
    that. And the platform was quite closed. Now it is easy to make very powerful queries</p>
    <p>We'll have google sign-in for player management. There will also be clan management.</p>
    <p>I'll also create a public UDP API to stream the live information we get from all sauer servers in a consolidated fashion.
    This will allow you to build your own apps on top including analysis. Likewise, you can consume the API already. See the GitHub
    repository for examples, including this very PHP website making use of the public API.</p>
    <p>With this project, we should have an open ecosystem where we can publish game information on blogs, websites...
    All automatically aggregated for everyone's use.</p>

    <h2>What is the history of the project?</h2>

    <ul>
        <li><strong>January 2014:</strong> Began working on an AssaultCube ladder, next-gen of <a href="http://hi-skill.us/">HI-SKILL</a></li>
        <li><strong>February 2014:</strong> Realised it's futile, so decided to focus on Sauerbraten as it's got a stronger player base.</li>
        <li><strong>April 2014:</strong> Most of the parsing logic was complete.</li>
        <li><strong>August 2014:</strong> Built the front-end.</li>
        <li><strong>September 2014:</strong> Released the project.</li>
        <li><strong>November 2014:</strong> Started working on AssaultCube again.</li>
        <li><strong>December 2015:</strong> Released woop.ac</li>
        <li><strong>January 2015:</strong> Start working on new features, such as player registration and individual stats.</li>
        <li><strong>February 2015:</strong> Released them features &amp; went back to woop.ac.</li>
        <li><strong>July 2015:</strong> Released an open API</li>
        <li><strong>October 2015:</strong> Decided to open-source it and re-architect it.</li>
    </ul>

</article>

    <?php echo $foot; ?>
