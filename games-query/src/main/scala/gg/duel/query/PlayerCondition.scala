package gg.duel.query

import gg.duel.SimpleGame
import gg.duel.query.PlayerConditionOperator.{Or, And}

/**
  * Created on 04/10/2015.
  */
sealed trait PlayerConditionOperator {
  def stringValue: String

  def isOr = this == Or

  def isAnd = this == And
}

object PlayerConditionOperator {
  def unapply(string: String): Option[PlayerConditionOperator] = PartialFunction.condOpt(string) {
    case Or.stringValue => Or
    case And.stringValue => And
  }

  case object And extends PlayerConditionOperator {
    override val stringValue: String = "and"
  }

  case object Or extends PlayerConditionOperator {
    override val stringValue: String = "or"
  }

}

sealed trait PlayerCondition extends (SimpleGame => Boolean) {
  def toMap: Map[String, Seq[String]]
}

object PlayerCondition {
  def apply(map: Map[String, Seq[String]]): Either[String, PlayerCondition] = {
    val player = map.get("player").toSet.flatten
    val user = map.get("user").toSet.flatten
    val clan = map.get("clan").toSet.flatten

    if (player.isEmpty && user.isEmpty && clan.isEmpty)
      Right(EmptyPlayerCondition)
    else {

      {
        map.get("operator").flatMap(_.lastOption) match {
          case Some(PlayerConditionOperator(operator)) => Right(operator)
          case Some(stuff) => Left("Invalid operator given")
          case None => Right(PlayerConditionOperator.Or)
        }
      }.right.map { condition =>
        PlayerConditionNonEmpty(
          player = player,
          user = user,
          clan = clan,
          playerConditionOperator = condition
        )
      }
    }
  }

}

case object EmptyPlayerCondition extends PlayerCondition {
  override def apply(game: SimpleGame) = true

  override def toMap = Map.empty
}

case class PlayerConditionNonEmpty(player: Set[String], user: Set[String], clan: Set[String], playerConditionOperator: PlayerConditionOperator)
  extends PlayerCondition {
  override def toMap = Map(
    "player" -> player.toList,
    "user" -> user.toList,
    "clan" -> clan.toList
  ) ++ {
    if (playerConditionOperator == PlayerConditionOperator.And) Option("operator" -> List(playerConditionOperator.stringValue)) else Option.empty
  }

  override def apply(game: SimpleGame): Boolean = {
    if (playerConditionOperator == Or) {
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

sealed trait TagFilter extends (SimpleGame => Boolean) {
  def toMap: Map[String, Seq[String]]
}
case object EmptyTagFilter extends TagFilter {
  override def apply(simpleGame: SimpleGame): Boolean = true
  override def toMap = Map.empty
}
case class NonEmptyTagFilter(tags: Set[String]) extends TagFilter {
  override def apply(simpleGame: SimpleGame): Boolean = {
    (tags -- simpleGame.tags).isEmpty
  }
  override def toMap = Map("tag" -> tags.toSeq)
}
object TagFilter {
  def apply(map: Map[String, Seq[String]]): TagFilter = {
    map.get("tag") match {
      case Some(tags) if tags.nonEmpty =>
        NonEmptyTagFilter(tags = tags.toSet)
      case _ => EmptyTagFilter
    }
  }
}

sealed trait ServerFilter extends (SimpleGame => Boolean) {
  def toMap: Map[String, Seq[String]]
  def matches(server: String): Boolean

  def apply(simpleGame: SimpleGame): Boolean = {
    matches(simpleGame.server)
  }
}

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
