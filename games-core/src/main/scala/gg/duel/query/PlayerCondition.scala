package gg.duel.query

import gg.duel.SimpleGame

/**
 * Created on 04/10/2015.
 */
sealed trait PlayerConditionOperator
case object And extends PlayerConditionOperator
case object Or extends PlayerConditionOperator
object PlayerConditionOperator {
  def unapply(string: String): Option[PlayerConditionOperator] = PartialFunction.condOpt(string) {
    case "or" => Or
    case "and" => And
  }
}

case class PlayerCondition(player: Set[String], user: Set[String], clan: Set[String], playerConditionOperator: PlayerConditionOperator)
extends (SimpleGame => Boolean) {
  override def apply(game: SimpleGame): Boolean = {
    {
      player.isEmpty &&
        user.isEmpty && clan.isEmpty
    } || {
      if ( playerConditionOperator == Or ) {
        (game.users & user).nonEmpty ||
          (game.players & player).nonEmpty ||
          (game.clans & clan).nonEmpty
      } else {
        (user.isEmpty || (user.nonEmpty && (user -- game.users).isEmpty)) &&
          (player.isEmpty || (player.nonEmpty && (player -- game.players).isEmpty)) &&
          (clan.isEmpty || (clan.nonEmpty && (clan -- game.clans).isEmpty))
      }
    }
  }
}

case class TagFilter(tags: Set[String]) extends (SimpleGame => Boolean) {
  override def apply(simpleGame: SimpleGame): Boolean = {
    tags.isEmpty || (tags.nonEmpty && (tags -- simpleGame.tags).isEmpty)
  }
}

sealed trait ServerFilter extends (SimpleGame => Boolean) {
  def matches(server: String): Boolean
  def apply(simpleGame: SimpleGame): Boolean = {
    matches(simpleGame.server)
  }
}
case class SimpleServerFilter(servers: Set[String]) extends ServerFilter {
  override def matches(server: String): Boolean = {
    servers contains server
  }
}
case object NoServerFilter extends ServerFilter {
  override def matches(server: String): Boolean = true
}
