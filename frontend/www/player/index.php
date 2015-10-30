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
<?php if ( !isset($_GET['name'])) { $_GET['name'] = "w00p|Drakas";} ?>
                <div id="dduelss">
                    <pre><?php
                        $url = "http://api.duel.gg/games/duel/recent/?player=".rawurlencode($_GET['name']);
                        if ( isset($_GET['before']) ) {
                            $url = "http://api.duel.gg/games/duel/focus/".rawurlencode((string)$_GET['before'])."/?previous=25&next=0&player=".rawurlencode($_GET['name']);
                        }
                        $recentduels = json_decode(file_get_contents($url), true);
                        if ( isset($recentduels['previous'])) $recentduels = $recentduels['previous'];
                        ?></pre>
                </div>

                <?php
                require_once("../render_game.inc.php");
                foreach($recentduels as $duel) {
                    render_duel($duel);
                }

                ?>
                <div id="rest">

                        <p><a href="?name=<?php echo rawurlencode($_GET['name']) ?>&amp;before=<?php echo rawurlencode($duel['startTimeText']);?>">Older games...</a></p>

                </div>

            </div>
        </div>

<?php echo $foot; ?>