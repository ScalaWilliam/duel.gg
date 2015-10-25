<?php
require("../render.inc.php");
?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

            <?php
            $game = json_decode(file_get_contents("http://alfa.duel.gg/api/game/".rawurlencode($_GET['id'])."/"), true);
            require_once("../render_game.inc.php");
            render_duel($game);
            ?>

        </div>
<?php echo $foot; ?>