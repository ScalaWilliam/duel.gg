<?php
require("../render.inc.php");
?>
            <?php
            $uri = "$host/games/game/".rawurlencode($_GET['id'])."/";
            show_api_endpoint($uri);
?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">
        <?php
            $game = json_decode(file_get_contents($uri), true);
            require_once("../render_game.inc.php");
            render_game($game);
            ?>

        </div>
<?php echo $foot; ?>