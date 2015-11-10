<?php
require("../render.inc.php");
?>
    <style scoped>ul { list-style-type:none; } #content { text-align: left }

        tbody tr:last-child td, tbody tr:last-child th {
            padding-bottom:2em;
        }
    </style>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

        <h2>API</h2>

        <p>I made an API which you can access freely via Scala, Java, JavaScript, Node.js, PHP, Python, ... take your
            pick. </p>

        <h3>Example use of Query Parameters and Limit</h3>
        <?php $link = $host . '/ctf/recent/?clan=rb&clan=woop&operator=and&type=clanctf&limit=10'; ?>
        <pre><code><a href="<?php echo htmlspecialchars($link); ?>"><?php echo htmlspecialchars($link); ?></a></code></pre>
        <h3>Duels JSON Schema</h3>
        <p>Using the <a target="_blank" href="http://json-schema.org/">JSON Schema standard</a>, you can use the
            <a target="_blank" href="https://github.com/ScalaWilliam/duel.gg/blob/master/api/test/resources/schema.json">Duel JSON Schema</a>
            .</p>

        <h3><code>QueryCondition</code></h3>
        <p>A list of query string parameters to narrow down your search results:</p>

        <ul>
            <li><code>[OPTIONAL] type = ctf | clanctf | duel | all</code>, default depends on endpoint.</li>
            <li><code>[OPTIONAL] server = &lt;server ip&gt;</code></li>
            <li><code>[OPTIONAL, MULTIPLE] tag = A, B, C</code></li>
            <li><code>[OPTIONAL] PlayerCondition</code> as per below</li>
        </ul>
        <h4><code>Player Condition</code></h4>
        <ul>
            <li><code>[OPTIONAL, MULTIPLE] player = A, B, C</code></li>
            <li><code>[OPTIONAL, MULTIPLE] user = A, B, C</code></li>
            <li><code>[OPTIONAL, MULTIPLE] clan = A, B, C</code></li>
            <li><code>[OPTIONAL] operator = and | or</code>, determine whether all of the above get combined with
                <code>OR</code> or <code>AND</code> boolean operators.</li>
        </ul>

        <table>
            <thead><tr><th>Endpoint</th><th>Description</th></tr></thead>
            <tbody>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/games/new/">/games/new/</code></th>
                    <td>EventSource new games. Supports <code>QueryCondition</code></td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/games/live/">/games/live/</code></th>
                    <td>EventSource new and live games. Supports <code>QueryCondition</code></td>
                </tr>
            </tbody>
            <tbody>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/games/game/2015-11-10T19:25:48Z/">/games/game/1/</code></th>
                    <td>Find JSON game by its ID</td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/games/?id=2015-11-10T19:25:48Z&amp;id=2015-11-08T20:10:48Z">/games/?id=1&amp;id=2...</code></th>
                    <td>Find JSON games by their IDs</td>
                </tr>
            </tbody>
            <tbody>
                <tr>
                    <th><code><a rel="nofollow" href="<?php echo $host; ?>/duels/raw/">/duels/raw/</code></th>
                    <td>Download all duels in a tab-separated format. Supports <code>QueryCondition</code>.</td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/duels/recent/">/duels/recent/</code></th>
                    <td>Recent duels as JSON. Supports <code>QueryCondition</code> and <code>LimitCondition</code>.</td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/duels/before/2015-11-10T20:01:00Z/">/duels/before/1/</code></th>
                    <td>Duels before <code>1</code> as JSON. Supports <code>QueryCondition</code> and <code>LimitCondition</code>.
                    This is to support sequential querying.</td>
                </tr>
            </tbody>
            <tbody>
                <tr>
                    <th><code><a rel="nofollow" href="<?php echo $host; ?>/ctf/raw/">/ctf/raw/</code></th>
                    <td>Download all CTFs in a tab-separated format. Supports <code>QueryCondition</code>.</td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/ctf/recent/">/ctf/recent/</code></th>
                    <td>Recent ctf as JSON. Supports <code>QueryCondition</code> and <code>LimitCondition</code>.</td>
                </tr>
                <tr>
                    <th><code><a href="<?php echo $host; ?>/ctf/before/2015-11-10T20:01:00Z/">/ctf/before/1/</code></th>
                    <td>CTF before <code>1</code> as JSON. Supports <code>QueryCondition</code> and <code>LimitCondition</code>.
                        This is to support sequential querying.</td>
                </tr>
            </tbody>
            <tbody>

            <tr>
                <th><code><a href="<?php echo $host; ?>/nicknames/">/nicknames/</code></th>
                <td>All nicknames as JSON.</td>
            </tr>
            <tbody>
            <tr>
                <th><code><a href="<?php echo $host; ?>/clans/">/clans/</code></th>
                <td>All clans as JSON.</td>
            </tr>
            </tbody>

        </table>
    </div>
<?php echo $foot; ?>