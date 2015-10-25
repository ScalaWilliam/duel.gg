declare option output:omit-xml-declaration 'no';
let $entries :=
for $article in /article
let $publish-time := xs:dateTime($article/@publish-time)
where $publish-time le current-dateTime()
where $article/@enabled = 'true'
order by $publish-time descending
let $article-link := "http://duel.gg/news/" || data($article/@name) || "/"
return <entry xmlns="http://www.w3.org/2005/Atom">
  <title>{data($article/@title)}</title>
  <link href="{$article-link}"/>
  <id>{$article-link}</id>
  <updated>{data($article/@publish-time)}</updated>
  <content type="html">{data($article)}</content>
  <author><name>Drakas</name></author>
</entry>
let $main-updated-time := max(
  for $article in /article
  return xs:dateTime($article/@publish-time)
)
return
<feed xmlns="http://www.w3.org/2005/Atom">
	<title>duel.gg News Feed</title>
	<subtitle>The Sauerbraten Duel League.</subtitle>
	<link href="http://duel.gg/news/atom/" rel="self" />
	<link href="http://duel.gg/" />
	<id>http://duel.gg/</id>
	<updated>{$main-updated-time}</updated>
 {$entries}</feed>