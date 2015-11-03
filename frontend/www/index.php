<?php
require("render.inc.php");
$host = "http://api.duel.gg";
//$host = "http://localhost:9000";
$uri = "$host/games/recent/?limit=5&type=duel";
if ( isset($_GET['before']) ) {
    $uri = "$host/games/before/".rawurlencode((string)$_GET['before'])."/?limit=20&type=duel";
}

$recentgames = json_decode(file_get_contents($uri), true);
if ( isset($recentgames['previous'])) $recentgames = $recentgames['previous'];
require_once("parse_link.inc.php");

$links = get_links();
$has_more = false;
foreach($links as $link) {
    if ( $link->title == 'Live game updates SSE stream' ) {
        $live_link = $host.$link->path;
        ?><span id="live-games-url" data-path="<?php echo $live_link; ?>"/><?php
    }
    if ( $link->rel == 'previous') {
        $has_more = true;
    }
}
show_api_endpoint($uri, $live_link);

?>
        <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

            <div id="first">
<?php



                        ?>

                <?php
                require_once("render_game.inc.php");
                foreach($recentgames as $game) {
                    render_game($game);
                }
                ?>

                <?php if ( $has_more ) { ?>
                <div id="rest">
                    <p><a href="?before=<?php echo rawurlencode($game['startTimeText']);?>">Older games...</a></p>
                </div>
    <?php } ?>

            </div>
        </div>

<?php ?>
<?php echo $foot; ?>