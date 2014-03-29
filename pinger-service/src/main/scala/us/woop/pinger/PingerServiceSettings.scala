package us.woop.pinger
import akka.actor.ActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import scala.concurrent.duration._
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
class PingerServiceSettingsImpl(config: Config) extends Extension {
  val subscribeToPingDelay = config.getDuration("us.woop.pinger.pinger-service.subscribe-to-ping-delay",
      TimeUnit.MILLISECONDS).millis
  val defaultPingInterval = config.getDuration("us.woop.pinger.pinger-service.default-ping-interval",
      TimeUnit.MILLISECONDS).millis
}
object PingerServiceSettings extends ExtensionId[PingerServiceSettingsImpl] with ExtensionIdProvider {

  override def lookup() = PingerServiceSettings

  override def createExtension(system: ExtendedActorSystem) =
    new PingerServiceSettingsImpl(system.settings.config)

  override def get(system: ActorSystem) = super.get(system)
}