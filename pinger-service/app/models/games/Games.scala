package models.games

import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel

/**
 * Created on 13/07/2015.
 */
object Games {
  def empty = Games(duels = Map.empty, ctfs = Map.empty)
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
}
