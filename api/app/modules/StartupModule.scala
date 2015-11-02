package modules

import play.api.{Configuration, Environment}
import play.api.inject._

class StartupModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[GamesService].toSelf.eagerly()
  )
}
