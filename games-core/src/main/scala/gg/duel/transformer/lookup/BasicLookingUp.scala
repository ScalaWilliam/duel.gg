package gg.duel.transformer.lookup

import gg.duel.transformer.GameNode

/**
* Created by William on 08/11/2015.
*/
class BasicLookingUp(demoLookup: GameNode => Option[String],
                     clanLookup: String => Option[String],
                     countryLookup: String => Option[(String, String)]) extends LookingUp {
  override def lookupDemo(gameNode: GameNode): Option[String] = demoLookup(gameNode)
  override def lookupClan(nickname: String): Option[String] = clanLookup(nickname)
  override def lookupCountryCodeAndName(ip: String): Option[(String, String)] = countryLookup(ip)
}
