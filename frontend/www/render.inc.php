<?php

function show_api_endpoint($uri) {
    ?><div id="api-endpoint">JSON API endpoint for this page: <code><a href="<?php echo htmlspecialchars($uri); ?>" target="_blank"><?php echo htmlspecialchars($uri); ?></a></code></div><?php
}
function verify_user() {
//    $_COOKIE['auth']
}

$main_template_path = dirname(__FILE__) . "/template.html";
$domdoc = new DOMDocument();
libxml_use_internal_errors(true);
$domdoc->loadHTMLFile($main_template_path);
libxml_use_internal_errors(false);
if ( isset($title) ) {
    $domdoc->getElementsByTagName("title")->item(0)->textContent = $title;
}

$split_text = "---SPLIT HERE---";
$content_node = $domdoc->getElementById("content");
$split_text_node = $domdoc->createTextNode($split_text);
$content_node->parentNode->replaceChild($split_text_node, $content_node);

list($head, $foot) = explode($split_text, $domdoc->saveHTML());
if ( !isset($skip_head) || $skip_head === false ) {
    echo $head;
}
