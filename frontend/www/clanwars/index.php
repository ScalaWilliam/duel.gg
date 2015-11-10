<?php
require("../render.inc.php");
require("../render_game.inc.php");
$uri = "$host/ctf/recent/?type=clanctf&limit=5";
if (isset($_GET['before'])) {
    $uri = "$host/ctf/before/" . rawurlencode((string)$_GET['before']) . "/?type=clanctf&limit=10";
}
require_once("../parse_link.inc.php");
$clanwars = json_decode(file_get_contents($uri), true);
$links = get_links();

show_api_endpoint($uri, null);

?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;"><?php

        foreach ($clanwars as $ctf) {
            render_ctf($ctf);
        }
        ?>        <?php if (isset($ctf)) { ?>
            <div id="rest">

                <p><a href="?before=<?php echo rawurlencode($ctf['startTime']); ?>">Older wars...</a></p>

            </div>


        <?php } ?>
    </div>
<?php echo $foot; ?>