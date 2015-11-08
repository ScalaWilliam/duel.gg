package modules

import play.api.{Configuration, Environment}
import play.api.inject._
import services.demos.{DemoCollectorLive, DemoCollectorEmpty, DemoCollection}
import services.games.GamesService
import services.live.{RabbitSourceRunning, NoRabbitSource, RabbitSource}

class StartupModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {

    if (environment.mode == play.api.Mode.Test) Seq.empty
    else
      Seq(
        bind[GamesService].toSelf.eagerly(),
        bind[DemoCollection].to {
          configuration.getBoolean("gg.duel.demo-collector.active") match {
            case Some(true) => classOf[DemoCollectorLive]
            case _ => classOf[DemoCollectorEmpty]
          }
        }.eagerly(),
        bind[RabbitSource].to {
          configuration.getBoolean("gg.duel.live-games.active") match {
            case Some(true) => classOf[RabbitSourceRunning]
            case _ => classOf[NoRabbitSource]
          }
        }.eagerly()
      )
  }
}
