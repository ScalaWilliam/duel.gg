<?php require("../render.inc.php"); ?>

<form method="post" enctype="multipart/form-data">
    <label>User ID: <input type="text" name="id" placeholder="enter your user ID (3 to 10 a-z characters)" pattern="[a-z]{3,10}"/></label>
    <label>Nickname: <input type="text" name="id" placeholder="enter your in-game nickname" pattern="[^-\s]{3,15}"/></label>
    <button type="submit">Register</button>
</form>

<?php echo $foot; ?>
