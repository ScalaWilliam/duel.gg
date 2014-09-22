package plugins

import akka.actor.Status.Failure
import akka.util.Timeout
import play.api.{Play, Application, Plugin}
import plugins.LeagueInterface.{Report, GiveMe, Refresh, NewStatsListing}

import scalax.io.JavaConverters._
class LeagueInterface(implicit app: Application) extends Plugin {

  lazy val getLeagueRecordXq = {
    app.resourceAsStream("/get-league-record.xq").get.asInput.string
  }

  def requestData(implicit timeout: Timeout) = {
    import akka.pattern.ask
    mainActor.ask(GiveMe).mapTo[Report]
  }

  import akka.actor.ActorDSL._
  implicit def sys = play.libs.Akka.system
  def refresh(): Unit = {
    mainActor ! Refresh
  }
  lazy val mainActor = actor(new Act {
    var currentListing: scala.xml.Elem = _
    whenStarting {
      import concurrent.duration._
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.schedule(0.2.seconds, 30.seconds, self, Refresh)
    }
    become {
      case GiveMe =>
        sender() ! Report(currentListing)
      case Failure(reason) =>
        println(s"failed: $reason")
      case Refresh =>
        import scala.concurrent.ExecutionContext.Implicits.global
        import akka.pattern.pipe
        DuelsInterface.duelsInterface.holder.post(<query xmlns="http://basex.org/rest">
          <text>
            {getLeagueRecordXq}
          </text>
        </query>).map(r => {
          NewStatsListing(r.xml)
        }) pipeTo self
      case NewStatsListing(listing) =>
        currentListing = listing
    }
  })

}
object LeagueInterface {
  case object GiveMe
case object Refresh
case class NewStatsListing(value: scala.xml.Elem)
  case class Report(value: scala.xml.Elem)
  def leagueInterface: LeagueInterface = Play.current.plugin[LeagueInterface]
    .getOrElse(throw new RuntimeException("LeagueInterface plugin not loaded"))
}
