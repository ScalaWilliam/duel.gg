package gg.duel.query

import gg.duel.query.QueryableGame$

/**
  * Created by William on 03/11/2015.
  */
case class QueryCondition(gameType: GameType, playerCondition: PlayerCondition,
                          tag: TagFilter, server: ServerFilter) extends (QueryableGame => Boolean) {
  override def apply(simpleGame: QueryableGame): Boolean = {
    gameType(simpleGame) && playerCondition(simpleGame) && tag(simpleGame) && server(simpleGame)
  }

  def toMap: Map[String, Seq[String]] = {
    gameType.toMap ++ playerCondition.toMap ++ tag.toMap ++ server.toMap
  }
}

object QueryCondition {
  def apply(m: Map[String, Seq[String]]): Either[String, QueryCondition] = {
    for {
      gameType <- GameType.apply(m).right
      playerCondition <- PlayerCondition.apply(m).right
    } yield {
      val tag = TagFilter.apply(m)
      val server = ServerFilter.apply(m)
      QueryCondition(
        gameType = gameType,
        playerCondition = playerCondition,
        tag = tag,
        server = server
      )
    }
  }
}