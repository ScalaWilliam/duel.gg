package services.games

import gg.duel.SimpleGame

/**
  * Created by William on 08/11/2015.
  */
object Games {
  def empty: Games = Games(
    games = Map.empty
  )
}

case class Games
(games: Map[String, SimpleGame]) {
  me =>
  def withNewGame(simpleGame: SimpleGame): Games = {
    copy(games = games + (simpleGame.id -> simpleGame))
  }

  def +(simpleGame: SimpleGame): Games = withNewGame(simpleGame)

  def ++(games: Games): Games = Games(
    games = me.games ++ games.games
  )
}

