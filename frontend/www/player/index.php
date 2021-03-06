<?php
require("../render.inc.php");
if (!isset($_GET['name'])) {
    $_GET['name'] = "w00p|Drakas";
}
$url = "$host/duels/recent/?limit=5&player=" . rawurlencode($_GET['name']);
if (isset($_GET['before'])) {
    $url = "$host/duels/before/".rawurlencode((string)$_GET['before'])."/?limit=10&player=" . rawurlencode($_GET['name']);
}
$recentduels = json_decode(file_get_contents($url), true);
require_once("../parse_link.inc.php");

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

show_api_endpoint($url, null);

?>
    <div id="content">
        <div id="first">

            <?php
            require_once("../render_game.inc.php");
            foreach ($recentduels as $duel) {
                render_game($duel);
            }

            ?>
            <?php if (isset($duel)) { ?>
                <div id="rest">

                    <p>
                        <a href="?name=<?php echo rawurlencode($_GET['name']) ?>&amp;before=<?php echo rawurlencode($duel['startTime']); ?>">Older
                            games...</a></p>


                </div>

            <?php } ?>
        </div>
    </div>
<?php echo $foot; ?>