<?php
require("../render.inc.php");
require("../render_game.inc.php");
?>
    <?php
    $uri = "http://api.duel.gg/games/ctf/recent/?tag=clanwar&limit=10";
    if ( isset($_GET['before']) ) {
        $uri = "http://api.duel.gg/games/ctf/focus/".rawurlencode((string)$_GET['before'])."/?tag=clanwar&previous=10&next=0";
    }
    show_api_endpoint($uri);

    ?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;"><?php
    $clanwars = json_decode(file_get_contents($uri), true);
    if ( isset($clanwars['previous'])) $clanwars = $clanwars['previous'];
    foreach($clanwars as $ctf) {
        render_ctf($ctf);
    }
    ?>        <?php if ( $ctf ) { ?>

        <div id="rest">

        <p><a href="?before=<?php echo rawurlencode($ctf['startTimeText']);?>">Older wars...</a></p>

    </div>


<?php } ?>
</div>
<?php echo $foot; ?>