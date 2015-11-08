package gg.duel.query

import gg.duel.query.QueryableGame$


case class SimpleServerFilter(servers: Set[String]) extends ServerFilter {
  override def matches(server: String): Boolean = {
    servers contains server
  }
  override def toMap = Map("server" -> servers.toSeq)
}

case object NoServerFilter extends ServerFilter {
  override def matches(server: String): Boolean = true
  override def toMap = Map.empty
}
object ServerFilter {
  def apply(map: Map[String, Seq[String]]): ServerFilter = {
    map.get("server") match {
      case Some(servers) if servers.nonEmpty => SimpleServerFilter(servers = servers.toSet)
      case _ => NoServerFilter
    }
  }
}

sealed trait ServerFilter extends (QueryableGame => Boolean) {
  def toMap: Map[String, Seq[String]]
  def matches(server: String): Boolean

  def apply(simpleGame: QueryableGame): Boolean = {
    matches(simpleGame.server)
  }
}
