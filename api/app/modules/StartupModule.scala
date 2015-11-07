package modules

import play.api.{Configuration, Environment}
import play.api.inject._

class StartupModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[GamesService].toSelf.eagerly(),
    bind[DemoCollection].to{configuration.getBoolean("gg.duel.demo-collector.active") match {
      case Some(true) => classOf[DemoCollectorLive]
      case _ =>  classOf[DemoCollectorEmpty]
    }}.eagerly(),
    bind[UpstreamGames].to{configuration.getBoolean("gg.duel.live-games.active") match {
      case Some(true) => classOf[UpstreamGamesLive]
      case _ => classOf[UpstreamGamesNone]
    }}
  )
}
