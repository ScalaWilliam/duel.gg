<?php
require("../basex.inc.php");
function list_articles() {
    $api = simplexml_load_file("tumblr.api-read.xml");
    $articles = [];
    $rss = simplexml_load_file("tumblr.rss.xml");
    foreach($api->posts->post as $post) {
        if ( preg_match('/.*\/post\/[0-9]+\/([a-zA-Z0-9-]+)$/', $post['url-with-slug'], $matches) ) {
            foreach($rss->channel->item as $item) {
                if ( (string)$item->link === (string)$post['url']) {
                    $articles[] = array(
                        "id" => $matches[1],
                        "title" => (string)$item->title
                    );
                }
            }
        }
    }
    return $articles;
}

function get_basex_article($article_id) {
    $article_query = '<rest:query xmlns:rest="http://basex.org/rest"><rest:text></rest:text><rest:variable name="article-name" value=""/></rest:query>';
    $article_query = simplexml_load_string($article_query);
    $article_query->text = file_get_contents(__DIR__ . "/view_item.xq");
    $article_query->variable['value'] = $article_id;
    return basex_query($article_query->asXML());
}

function get_tumblr_article($article_id) {
    $api = simplexml_load_file("tumblr.api-read.xml");
    $rss = simplexml_load_file("tumblr.rss.xml");
    foreach($api->posts->post as $post) {
        if ( preg_match('/.*\/post\/[0-9]+\/([a-zA-Z0-9-]+)$/', $post['url-with-slug'], $matches) ) {
            if ( $matches[1] === $article_id ) {
                foreach($rss->channel->item as $item) {
                    if ( (string)$item->link === (string)$post['url']) {
                        return "<article class=\"tumblr_article\"><h2>".htmlspecialchars($item->title)."</h2>".
                        $item->description.
                        "<p><a target=\"_blank\" href=\"".htmlspecialchars($post['url-with-slug'])."\">View on Tumblr</a></p></article>";
                    }
                }
            }
        }
    }
}

