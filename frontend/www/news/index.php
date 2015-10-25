<?php
$skip_head = true;
require("../render.inc.php");
require("news.inc.php");
if ( preg_match('/.*\/news\/$/', $_SERVER['REQUEST_URI']) ) {
    echo $head;
    ?><ol><?php
    foreach(list_articles() as $article) {
        ?> <li><a href="/news/<?php echo $article['id']; ?>/"><?php echo $article['title']; ?></a></li><?php
    }
        ?></ol><?php
} else if (preg_match('/.*\/news\/([a-zA-Z0-9-]+)\/$/', $_SERVER['REQUEST_URI'], $matches)) {
    $article_id = $matches[1];
    if ( $basex_article = get_basex_article($article_id) ) {
        echo $head; echo $basex_article;
    } else if ( $tumblr_article = get_tumblr_article($article_id) ) {
        echo $head; echo $tumblr_article;
    } else {
        header("HTTP/1.1 404 Not Found");
        ?><p>Not found</p><?php
    }
}

?>

<?php echo $foot;