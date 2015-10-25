<?php require("../render.inc.php") ?>
<div id="content">
            <div id="first">
                <style scoped>
                    #news header {
                        display:flex;
                        margin-bottom:0.6em;
                    }
                    #news h2 {
                        margin-top:0;
                        margin-bottom: 0;
                    }
                    #news time {
                        flex:1;
                        align-self:flex-end;
                        margin-bottom:0.15em;
                        margin-left:0.6em;
                    }
                </style>
                <br/>

                <div id="dduelss">
                    <pre><?php
    $url = "http://alfa.duel.gg/api/duels/recent/?player=".rawurlencode($_GET['name']);
                        if ( isset($_GET['to'])) {
                            $url = "http://alfa.duel.gg/api/duels/to/".rawurlencode($_GET['to'])."/?player=" . rawurlencode($_GET['name']);
                        }
                        $recentduels = json_decode(file_get_contents($url), true);

                        ?></pre>
                </div>

                <?php
                require_once("../render_game.inc.php");
                foreach($recentduels as $duel) {
                    render_duel($duel);
                }

                ?>
                <?php if ( isset($duel)) {
?>
                <p><a href="?name=<?php echo rawurlencode($_GET['name']); ?>&amp;to=<?php echo rawurlencode($duel['startTimeText']); ?>">Earlier games</a></p>
<?php } ?>
                <div id="rest">

                </div>

            </div>
        </div>

<?php echo $foot; ?>