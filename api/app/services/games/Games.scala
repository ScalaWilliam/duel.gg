package services.games

import gg.duel.query.QueryableGame

/**
  * Created by William on 08/11/2015.
  */
object Games {
  def empty: Games = Games(
    games = Map.empty
  )
}

case class Games
(games: Map[String, QueryableGame]) {
  me =>
  def withNewGame(simpleGame: QueryableGame): Games = {
    copy(games = games + (simpleGame.id -> simpleGame))
  }

  def +(simpleGame: QueryableGame): Games = withNewGame(simpleGame)

  def ++(games: Games): Games = Games(
    games = me.games ++ games.games
  )
}

