<?php require("../render.inc.php") ?>
<div id="content">
            <div id="first">
<?php if ( !isset($_GET['name'])) { $_GET['name'] = "w00p|Drakas";} ?>
                    <?php
                        $url = "http://api.duel.gg/games/all/recent/?player=".rawurlencode($_GET['name']);
                        if ( isset($_GET['before']) ) {
                            $url = "http://api.duel.gg/games/all/focus/".rawurlencode((string)$_GET['before'])."/?previous=25&next=0&player=".rawurlencode($_GET['name']);
                        }
                    show_api_endpoint($url);
                        $recentduels = json_decode(file_get_contents($url), true);
                        if ( isset($recentduels['previous'])) $recentduels = $recentduels['previous'];
                        ?>

                <?php
                require_once("../render_game.inc.php");
                foreach($recentduels as $duel) {
                    render_game($duel);
                }

                ?>
                <?php if ( $duel) { ?>
                <div id="rest">

                        <p><a href="?name=<?php echo rawurlencode($_GET['name']) ?>&amp;before=<?php echo rawurlencode($duel['startTimeText']);?>">Older games...</a></p>


                </div>

    <?php } ?>
            </div>
        </div>
<?php echo $foot; ?>