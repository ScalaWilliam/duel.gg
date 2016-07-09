(: Using BaseX 8.5 GUI :)
let $clans :=
(:let $html := html:parse(file:read-text("Downloads/Clans – Sauerworld.htm")):)
let $html := html:parse(http:send-request(
  <http:request method='get'
     override-media-type='application/octet-stream'
     href='http://www.sauerworld.org/clans/'>
    <http:header name="User-Agent" value="Opera"/>
  </http:request>
)[2])
for $p in $html//p[b]
for $ul in ($p/following-sibling::*)[1]/self::ul
let $clanname := substring-before($p/b, " – ")
let $clantag := substring-after($p/b, " – ")
let $website := string(($ul//a[not(contains(@href, "webchat"))])[1])
where not(empty($clanname)) and not(empty($clantag))
return <clan><name>{$clanname}</name>
<tag>{$clantag}</tag>
{if ( not($website = '') ) then ( <website>{$website}</website>) else ()}
</clan>

return csv:serialize(<csv>{$clans}</csv>, map{'separator': 'tab'})
