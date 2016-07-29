package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.duel.ZIteratorState
import gg.duel.pinger.data.Server

/**
  * Created by me on 29/07/2016.
  */
sealed trait ServerStates {
  def updated(server: Server, zIteratorState: ZIteratorState): ServerStates

  def get(server: Server): Option[ZIteratorState]
}

object ServerStates {
  case class ServerStatesImmutable(map: Map[Server, ZIteratorState]) extends ServerStates {
    def updated(server: Server, zIteratorState: ZIteratorState): ServerStates =
      copy(map = map.updated(server, zIteratorState))

    def get(server: Server): Option[ZIteratorState] = map.get(server)
  }

  class ServerStatesMutable extends ServerStates {
    private val map: java.util.Map[Server, ZIteratorState] = new java.util.HashMap()

    override def updated(server: Server, zIteratorState: ZIteratorState): ServerStates = {
      map.put(server, zIteratorState)
      this
    }

    override def get(server: Server): Option[ZIteratorState] = {
      Option(map.get(server))
    }
  }

  def immutable: ServerStates = ServerStatesImmutable(Map.empty)

  def mutable: ServerStates = new ServerStatesMutable
}
