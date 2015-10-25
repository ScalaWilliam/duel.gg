declare option output:omit-xml-declaration 'no';
<articles>{
  for $article in /article
  let $publish-time := xs:dateTime($article/@publish-time)
  where $publish-time le current-dateTime()
  where $article/@enabled = 'true'
  order by $publish-time descending
  return $article
}</articles>