<?php
$_SERVER['API_API_KEY'] = "test";
if (!isset($_SERVER['API_API_KEY'])) {
    die("Not ready to register");
}
$auth_cookie = $_COOKIE['auth'];
list($json, $_) = explode(":", $auth_cookie);
$authenticated = json_decode(base64_decode(hex2bin($json)));

if (isset($_POST['id'], $_POST['nickname']) && !isset($authenticated->id) && is_string($_POST['id']) && is_string($_POST['nickname'])) {
    $id = $_POST['id'];
    $nickname = $_POST['id'];
    $url = 'http://localhost:9001/web/register/?auth_token=' . $_SERVER['API_API_KEY'];
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    $post_items = array('id' => $id, 'nickname' => $nickname, 'authToken' => $auth_cookie);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_items);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($ch);
    echo "Result: $result";
    die("Done");
}

require("../render.inc.php"); ?>

<?php


?>
<form method="post" enctype="multipart/form-data">
    <label>User ID: <input type="text" name="id" placeholder="enter your user ID (3 to 10 a-z characters)" pattern="[a-z]{3,10}"/></label>
    <label>Nickname: <input type="text" name="nickname" placeholder="enter your in-game nickname"
                            pattern="[^-\s]{3,15}"/></label>
    <label>Country code: <strong><?php echo $authenticated->countryCode; ?></strong></label>
    <button type="submit">Register</button>
</form>

<?php echo $foot; ?>
