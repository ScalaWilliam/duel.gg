package modules

import play.api.{Environment, Configuration}
import play.api.inject._
import services.RabbitMQSinkService

class RabbitMQSinkModule extends Module {
  override def bindings(environment: Environment,
                        configuration: Configuration) = Seq(
    bind[RabbitMQSinkService].toSelf.eagerly()
  )
}