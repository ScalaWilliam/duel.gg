package gg.duel.pinger.mulprocess

import gg.duel.pinger.analytics.duel.ZIteratorState.{ZFoundDuel, ZInDuelState}
import gg.duel.pinger.analytics.duel.{LiveDuel, TransitionalBetterDuel, ZIteratorState}
import gg.duel.pinger.data.Server

case class Event(data: String, id: Option[String], name: Option[String])

case class LiveProcessor(liveGames: Map[Server, LiveDuel]) {
  def cleanUp: Option[(List[Event], LiveProcessor)] = {
    val removeServers = liveGames.toList.collect { case (server, ld)
      // remove stuff that's been out of date for 15 minutes or so
      if ((System.currentTimeMillis - ld.startTime) / 1000) / 60 >= 15 =>
      server
    }.toSet
    PartialFunction.condOpt(removeServers) {
      case rs if rs.nonEmpty =>
        val events = removeServers.toList.map { server =>
          liveGames(server) match {
            case liveDuel =>
              Event(
                data = liveDuel.toJson,
                id = Option(liveDuel.startTimeText),
                name = Option("live-duel-gone")
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
          val newLiveGames = liveGames.updated(server, liveDuel)
          val updateEvent = Event(
            data = liveDuel.toJson,
            id = Option(liveDuel.startTimeText),
            name = Option("live-duel")
          )
          Option(updateEvent) -> copy(liveGames = newLiveGames)
        }
      case ZFoundDuel(_, scd) =>
        val newEvent = Event(
          data = scd.toJson,
          id = Option(scd.startTimeText),
          name = Option("duel")
        )
        Option {
          Option(newEvent) -> copy(liveGames = liveGames - server)
        }
      case _: ZIteratorState if liveGames.contains(server) =>
        liveGames(server) match {
          case liveDuel =>
            val newEvent = Event(
              data = liveDuel.toJson,
              id = Option(liveDuel.startTimeText),
              name = Option("live-duel-gone")
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
