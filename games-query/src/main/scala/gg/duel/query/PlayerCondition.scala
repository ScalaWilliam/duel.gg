package gg.duel.query

import gg.duel.query.PlayerConditionOperator.{Or, And}
import gg.duel.query.QueryableGame$

/**
  * Created on 04/10/2015.
  */

sealed trait PlayerCondition extends (QueryableGame => Boolean) {
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
  override def apply(game: QueryableGame) = true

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

  override def apply(game: QueryableGame): Boolean = {
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

