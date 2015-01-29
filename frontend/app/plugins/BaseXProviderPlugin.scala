package plugins

import play.api._
import play.api.libs.ws.{WS, WSAuthScheme}
import scala.xml.Elem

class BasexProviderPlugin(implicit app: Application) extends Plugin {
  def dbName = Play.current.configuration.getString("basex.dbname").getOrElse{throw new RuntimeException("No basex username!")}
  def url = Play.current.configuration.getString("basex.url").getOrElse{throw new RuntimeException("No basex url!")}
  def username = Play.current.configuration.getString("basex.username").getOrElse{throw new RuntimeException("No basex username!")}
  def password = Play.current.configuration.getString("basex.password").getOrElse{throw new RuntimeException("No basex password!")}

  def query(xml: Elem) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      r <- WS.url(url).withAuth(username, password, WSAuthScheme.BASIC).post(xml)
      _ = if ( r.status != 200 ) { throw new RuntimeException(s"Expected status 200, got ${r.status}. Content: ${r.body}, query: $xml")}
    } yield r
  }

  def queryOption(xml: Elem) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for { r <- query(xml) } yield Option(r.body).filter(_.nonEmpty)
  }

  // todo onstart - check that it connects

}

object BasexProviderPlugin {
  def awaitPlugin: BasexProviderPlugin = Play.current.plugin[BasexProviderPlugin]
    .getOrElse(throw new RuntimeException("BasexProviderPlugin plugin not loaded"))
}