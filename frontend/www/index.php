<?php
require("render.inc.php");
$uri = "$host/duels/recent/?limit=5";
if ( isset($_GET['before']) ) {
    $uri = "$host/duels/before/".rawurlencode((string)$_GET['before'])."/?limit=20";
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
show_api_endpoint($uri);

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

                <div id="rest">
                    <p><a href="?before=<?php echo rawurlencode($game['startTime']);?>">Older games...</a></p>
                </div>

            </div>
        </div>

<?php ?>
<?php echo $foot; ?>