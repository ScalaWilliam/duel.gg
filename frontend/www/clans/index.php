<?php
require("../render.inc.php");
?>
    <?php show_api_endpoint("http://api.duel.gg/clans/"); ?>
    <div id="content" style="width:60em;margin-left:auto;margin-right:auto;">

        <?php
        function render_clan($clan) {
            ?>
            <section class="clan">
                <h2>[<?php echo $clan['id']; ?>] <a href="<?php echo htmlspecialchars($clan['website']); ?>" target="_blank"><?php echo htmlspecialchars($clan['name']); ?></a>
                    <?php if ( $clan['irc'] ) {
                        ?><a href="https://webchat.gamesurge.net/?channels=<?php echo rawurlencode(substr($clan['irc'], 1)); ?>" target="_blank">
                        <?php echo htmlspecialchars($clan['irc']); ?></a><?php } ?>
                </h2>
            </section>
            <?php
        }
        $clans = yaml_parse_file("clans.yml");
        foreach($clans as $id => $clan) {
            render_clan($clan);
        }

        ?>
    </div>
<?php echo $foot; ?>