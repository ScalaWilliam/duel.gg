package modules

import javax.inject._

import play.api.Configuration

/**
 * Created on 28/08/2015.
 */
@Singleton
class AuthenticationService @Inject()(configuration: Configuration) {
  import scala.collection.JavaConverters._
  val apiKeys = configuration.getStringList("gg.duel.api.api-key").toList.flatMap(_.asScala)
  def authenticates(apiKey: String): Boolean = apiKeys.contains(apiKey)
}
