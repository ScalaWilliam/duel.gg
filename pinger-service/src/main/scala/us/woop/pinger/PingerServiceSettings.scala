package us.woop.pinger
import akka.actor.ActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import scala.concurrent.duration._
import com.typesafe.config.{ConfigFactory, Config}
import java.util.concurrent.TimeUnit
class PingerServiceSettingsImpl(config: Config) extends Extension {
  lazy val subscribeToPingDelay = config.getDuration("us.woop.pinger.pinger-service.subscribe-to-ping-delay",
      TimeUnit.MILLISECONDS).millis
  lazy val defaultPingInterval = config.getDuration("us.woop.pinger.pinger-service.default-ping-interval",
      TimeUnit.MILLISECONDS).millis
}
object PingerServiceSettings extends ExtensionId[PingerServiceSettingsImpl] with ExtensionIdProvider {

  override def lookup() = PingerServiceSettings

  override def createExtension(system: ExtendedActorSystem) =
    new PingerServiceSettingsImpl(system.settings.config.withFallback(ConfigFactory.systemProperties()))

  override def get(system: ActorSystem) = super.get(system)
}