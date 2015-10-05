package gg.duel.query

/**
 * Created on 04/10/2015.
 */

sealed trait GameTypeCondition


case object AllGames extends GameTypeCondition

case object CtfOnly extends GameTypeCondition

case object DuelOnly extends GameTypeCondition

case object ClanwarsOnly extends GameTypeCondition
