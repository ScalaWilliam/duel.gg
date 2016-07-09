package gg.duel.pingerservice

import gg.duel.pinger.data.Server

/**
 * Created on 13/07/2015.
 */
object Servers {
  def empty = Servers(servers = Map.empty)
}

case class Servers(servers: Map[String, Server]) {

  def include(name: String, server: Server) =
    copy(servers = servers + (name -> server))

  def exclude(name: String) =
    copy(servers = servers - name)

}
