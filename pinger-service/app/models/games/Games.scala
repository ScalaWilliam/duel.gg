package models.games

import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel

/**
 * Created on 13/07/2015.
 */
object Games {
  def empty = Games(duels = Map.empty, ctfs = Map.empty)
}

case class GamesIndex(duels: List[String], ctfs: List[String])

case class Duels(duels: List[SimpleCompletedDuel])

case class Ctfs(ctfs: List[SimpleCompletedCTF])

case class CombinedGames(games: List[Either[SimpleCompletedDuel, SimpleCompletedCTF]]) {
  def range(from: Long, to: Long) = {
    CombinedGames(
      games.filter { g =>
        val t = g.fold(_.startTime, _.startTime)
        t >= from && t <= to
      }
    )
  }
  def latest(n: Int) = copy(games = games.takeRight(n))
  def reverse = copy(games = games.reverse)
}

case class Games(duels: Map[String, SimpleCompletedDuel],
                 ctfs: Map[String, SimpleCompletedCTF]
                  ) {
  def withDuel(duel: SimpleCompletedDuel): Games = {
    copy(duels = duels + (duel.simpleId -> duel))
  }

  def withCtf(ctf: SimpleCompletedCTF): Games = {
    copy(ctfs = ctfs + (ctf.simpleId -> ctf))
  }

  def asCombined = CombinedGames(
    (duels.mapValues(Left.apply) ++ ctfs.mapValues(Right.apply)).values.toList
      .sortBy(_.fold(_.startTime, _.startTime))
  )
}
