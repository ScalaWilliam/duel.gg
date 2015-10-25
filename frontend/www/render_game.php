<?php
function render_duel($duel) {
    ?>
    <section class="duel">
        <header>
            <h3 class="mode-map"><?php echo $duel['mode']; ?> @ <?php echo $duel['map']; ?></h3>
            <h2 class="date-time"><a href="/game/?id=<?php echo $duel['startTimeText']; ?>">
                    <time is="relative-time" datetime="<?php echo $duel['endTimeText']; ?>"
                        ><?php echo $duel['endTimeText']; ?></time></a></h2>
        </header>
        <section class="score score-left">
            <?php $player = $duel['players'][array_keys($duel['players'])[0]]; ?>
            <p class="score"><?php echo $player['frags']; ?></p>
            <p class="name"><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo $player['name']; ?></a></p>
        </section>
        <section class="score score-right">
            <?php $player = $duel['players'][array_keys($duel['players'])[1]]; ?>
            <p class="score"><?php echo $player['frags']; ?></p>
            <p class="name"><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo $player['name']; ?></a></p>
        </section>

    </section>
<?php
}