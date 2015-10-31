<?php
require("render.inc.php");
?>
<?php

$uri = "http://api.duel.gg/games/duel/recent/?limit=5";
if ( isset($_GET['before']) ) {
    $uri = "http://api.duel.gg/games/duel/focus/".rawurlencode((string)$_GET['before'])."/?previous=25&next=0";
}
show_api_endpoint($uri);

?>
        <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

            <div id="first">
<?php
$recentgames = json_decode(file_get_contents($uri), true);
                        if ( isset($recentgames['previous'])) $recentgames = $recentgames['previous'];


                        ?>

                <?php
                require_once("render_game.inc.php");
                foreach($recentgames as $game) {
                    render_game($game);
                }
                ?>

                <div id="rest">
                    <p><a href="?before=<?php echo rawurlencode($game['startTimeText']);?>">Older games...</a></p>
                </div>

            </div>
        </div>

<?php ?>
<?php echo $foot; ?>