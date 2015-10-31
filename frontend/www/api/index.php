<?php
require("../render.inc.php");
?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

        <h2>API</h2>

        <p>I made an API which you can access freely via Scala, Java, JavaScript, Node.js, PHP, Python, ... take your pick. </p>

        <h3>From <a href="https://github.com/ScalaWilliam/duel.gg/blob/master/README.md" target="_blank">GitHub README</a>:</h3>

        <h3><a id="user-content-games-api" class="anchor" href="#games-api" aria-hidden="true"><span class="octicon octicon-link"></span></a>games api</h3>

        <p>A game ID is its start time, ie startTimeText.</p>
<style scoped>td { text-align:left; }</style>
        <table><thead>
            <tr>
                <th>Endpoint</th>
                <th>Function</th>
            </tr>
            </thead><tbody>
            <tr>
                <td><a href="http://api.duel.gg/new-games/">/new-games/</a></td>
                <td><strong>EventSource / Server-Sent Events</strong> - automatic push of new games coming through.</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/duel/first/?player=vaQ%27Frosty&amp;limit=2">/games/duel/first/?player=vaQ%27Frosty&amp;limit=2</a></td>
                <td>find first duel games involving Drakas. Get two.</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/all/recent/?player=vaQ%27Frosty&amp;player=w00p%7Craffael&amp;operator=and">/games/all/recent/?player=vaQ%27Frosty&amp;player=w00p%7Craffael&amp;operator=and</a></td>
                <td>Find recent games with both raffael and Frosty</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/ctf/recent/?clan=rb&amp;clan=woop&amp;operator=or">/games/ctf/recent/?clan=rb&amp;clan=woop&amp;operator=or</a></td>
                <td>Find recent ctf games with either woop or rb clan</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael">/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael</a></td>
                <td>Find raffael's ctf games at the specified time, the game before and the game after</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael">/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael&amp;radius=15</a></td>
                <td>Find raffael's ctf games at the specified time, 15 games before, 15 after</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael">/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%7Craffael&amp;previous=0&amp;next=25</a></td>
                <td>Find raffael's ctf games at the specified time, 0 games before, 25 games after</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/game/2015-08-11T20:47:11Z/">/game/2015-08-11T20:47:11Z/</a></td>
                <td>get game at a specific ID</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/games/?game=2015-08-11T20:47:11Z&amp;game=2015-08-11T20:58:29Z">/games/?game=2015-08-11T20:47:11Z&amp;game=2015-08-11T20:58:29Z</a></td>
                <td>get these games</td>
            </tr>
            </tbody></table>

        <h3><a id="user-content-players-api" class="anchor" href="#players-api" aria-hidden="true"><span class="octicon octicon-link"></span></a>players api</h3>

        <table><thead>
            <tr>
                <th>Endpoint</th>
                <th>Function</th>
            </tr>
            </thead><tbody>
            <tr>
                <td><a href="http://api.duel.gg/nicknames/">/nicknames/</a></td>
                <td>list all seen nicknames</td>
            </tr>
            <tr>
                <td><a href="http://api.duel.gg/clans/">/clans/</a></td>
                <td>list all clans</td>
            </tr>
            </tbody></table>



    </div>
<?php echo $foot; ?>