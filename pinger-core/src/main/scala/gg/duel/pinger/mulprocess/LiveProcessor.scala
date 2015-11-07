package gg.duel.pinger.mulprocess

import gg.duel.pinger.analytics.ctf.{TransitionalCtf, CtfState}
import gg.duel.pinger.analytics.ctf.data.LiveCTF
import gg.duel.pinger.analytics.duel.{TransitionalBetterDuel, LiveDuel}
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker._
import gg.duel.pinger.data.Server

case class Event(data: String, id: Option[String], name: Option[String]) 
case class LiveProcessor(liveGames: Map[Server, Either[LiveDuel, LiveCTF]]) {
  def cleanUp: Option[(List[Event], LiveProcessor)] = {
    val removeServers = liveGames.toList.collect { case (server, ld)
      // remove stuff that's been out of date for 15 minutes or so
      if ((System.currentTimeMillis - ld.fold(_.startTime, _.startTime)) / 1000) / 60 >= 15 =>
      server
    }.toSet
    PartialFunction.condOpt(removeServers) {
      case rs if rs.nonEmpty =>
        val events = removeServers.toList.map{ server =>
          liveGames(server) match {
            case Left(liveDuel) =>
              Event(
                data = liveDuel.toJson,
                id = Option(liveDuel.startTimeText),
                name = Option("live-duel-gone")
              )
            case Right(liveCtf) =>
              Event(
                data = liveCtf.toJson,
                id = Option(liveCtf.startTimeText),
                name = Option("live-ctf-gone")
              )
          }
        }
        (events, copy(liveGames = liveGames -- rs))
    }
  }
  def stateChange(server: Server, previousState: ZIteratorState, currentState: ZIteratorState): Option[(Option[Event], LiveProcessor)] = {
    currentState match {
      case ZInDuelState(_, td: TransitionalBetterDuel) =>
        td.liveDuel.toOption.map { liveDuel => 
          val newLiveGames = liveGames.updated(server, Left(liveDuel))
          val updateEvent = Event(
            data = liveDuel.toJson,
            id = Option(liveDuel.startTimeText),
            name = Option("live-duel")
          )
          Option(updateEvent) -> copy(liveGames = newLiveGames)
        }
      case ZInCtfState(_, ctfState: TransitionalCtf) =>
        Option.empty
      case ZFoundDuel(_, scd) =>
        val newEvent = Event(
          data = scd.toJson,
          id = Option(scd.startTimeText),
          name = Option("duel")
        )
        Option { Option(newEvent) -> copy(liveGames = liveGames - server) }
      case ZFoundCtf(_, scc) =>
        val newEvent = Event(
          data = scc.toJson,
          id = Option(scc.startTimeText),
          name = Option("ctf")
        )
        Option { Option(newEvent) -> copy(liveGames = liveGames - server) }
      case _: ZIteratorState if liveGames.contains(server) =>
        liveGames(server) match {
          case Left(liveDuel) =>
            val newEvent = Event(
              data = liveDuel.toJson,
              id = Option(liveDuel.startTimeText),
              name = Option("live-duel-gone")
            )
            Option {
              Option(newEvent) -> copy(liveGames = liveGames - server)
            }
          case Right(liveCtf) =>
            val newEvent = Event(
              data = liveCtf.toJson,
              id = Option(liveCtf.startTimeText),
              name = Option("live-ctf-gone")
            )
            Option {
              Option(newEvent) -> copy(liveGames = liveGames - server)
            }
        }
      case _ => Option.empty
    }
  }
}
object LiveProcessor {
  def empty = LiveProcessor(liveGames = Map.empty)
}