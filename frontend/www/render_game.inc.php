<?php
function render_game($game) {
    if ( $game['type'] == 'ctf' ) {
        render_ctf($game);
    } else if ( $game['type'] == 'duel') {
        render_duel($game);
    }
}
function render_duel($duel) {
    ?>
    <section class="duel">
        <header>
            <h3 class="mode-map"><?php echo $duel['mode']; ?> @ <?php echo $duel['map']; ?></h3>
            <h2 class="date-time"><a href="/game/?id=<?php echo $duel['startTimeText']; ?>">
                    <time is="relative-time" datetime="<?php echo $duel['endTime']; ?>"
                        ><?php echo $duel['endTime']; ?></time></a></h2>
        </header>
        <section class="score score-left">
            <?php $player = $duel['players'][0]; ?>
            <p class="score"><?php echo $player['frags']; ?></p>
            <p class="name"><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo htmlspecialchars($player['name']); ?></a></p>
        </section>
        <section class="score score-right">
            <?php $player = $duel['players'][1]; ?>
            <p class="score"><?php echo $player['frags']; ?></p>
            <p class="name"><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo htmlspecialchars($player['name']); ?></a></p>
        </section>

        <?php show_demo($duel); ?>

    </section>
<?php
}

function show_demo($duel)
{
    if (isset($duel['demo'])) { ?><p class="demo"><a target="_blank"
                                                     href="<?php echo htmlspecialchars($duel['demo']); ?>">Download
            demo</a></p><?php
    }
}


function render_ctf($ctf) {
    ?>

    <section class="ctf">
        <header>
            <h3 class="mode-map"><?php echo $ctf['mode']; ?> @ <?php echo $ctf['map']; ?></h3>
            <h2 class="date-time"><a href="/game/?id=<?php echo $ctf['startTimeText']; ?>">
                    <time is="relative-time" datetime="<?php echo $ctf['endTime']; ?>"
                        ><?php echo $ctf['endTime']; ?></time></a></h2>
        </header>
        <section class="score score-left">
            <?php $team = $ctf['teams'][array_keys($ctf['teams'])[0]]; ?>
            <p class="score"><?php echo $team['flags']; ?></p>
            <p class="name"><?php echo $team['name'];?><br/>
            <span class="players">
                <?php foreach($team['players'] as $player) {
                    ?><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo htmlspecialchars($player['name']); ?></a><br/><?php
                } ?>
            </span></p>
        </section>
        <section class="score score-right">
            <?php $team = $ctf['teams'][array_keys($ctf['teams'])[1]]; ?>
            <p class="score"><?php echo $team['flags']; ?></p>
            <p class="name"><?php echo $team['name'];?><br/>
            <span class="players">
                <?php foreach($team['players'] as $player) {
                    ?><a href="/player/?name=<?php echo rawurlencode($player['name']); ?>"><?php echo htmlspecialchars($player['name']); ?></a><br/><?php
                } ?>
            </span></p>        </section>

        <?php show_demo($ctf); ?>

    </section>
<?php
}