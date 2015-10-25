declare option output:method 'raw';
declare variable $article-name as xs:string external;
let $cgh := 'CONTENT-GOES-HERE'
for $article in (/article[@name = $article-name])[1]
where $article/@enabled = 'true'
where xs:dateTime($article/@publish-time) lt current-dateTime()
let $art :=
<article>
<header>
<h2>{data($article/@title)}</h2>
<time is="relative-time" datetime="{data($article/@publish-time)}">{data($article/@publish-time)}</time>
</header>
<div class="content">{$cgh}</div>
</article>
return replace(serialize($art), $cgh, string($article))