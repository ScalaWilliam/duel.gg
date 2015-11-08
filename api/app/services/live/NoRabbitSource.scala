package services.live

import javax.inject._

import play.api.Logger

class NoRabbitSource @Inject()() extends RabbitSource {
  Logger.info("Rabbit games not active")
}
