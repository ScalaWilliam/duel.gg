<?php
require("../render.inc.php");
require("../render_game.inc.php");
$uri = "$host/games/recent/?tag=clanwar&limit=5";
if (isset($_GET['before'])) {
    $uri = "$host/games/before/" . rawurlencode((string)$_GET['before']) . "/?tag=clanwar&limit=10";
}
require_once("../parse_link.inc.php");
$clanwars = json_decode(file_get_contents($uri), true);
$links = get_links();
$has_more = false;
foreach ($links as $link) {
    if ($link->title == 'Live game updates SSE stream') {
        $live_link = $host . $link->path;
        ?><span id="live-games-url" data-path="<?php echo $live_link; ?>"/><?php
    }
    if ( $link->rel == 'previous') {
        $has_more = true;
    }
}

show_api_endpoint($uri, $live_link);

?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;"><?php

        foreach ($clanwars as $ctf) {
            render_ctf($ctf);
        }
        ?>        <?php if ($has_more && isset($ctf)) { ?>
            <div id="rest">

                <p><a href="?before=<?php echo rawurlencode($ctf['startTimeText']); ?>">Older wars...</a></p>

            </div>


        <?php } ?>
    </div>
<?php echo $foot; ?>