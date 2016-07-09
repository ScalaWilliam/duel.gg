package gg.duel.enricher.lookup

import gg.duel.enricher.{ClanResource, GameNode}

/**
  * Created by William on 08/11/2015.
  */
case class BasicLookingUp(demoLookup: GameNode => Option[String],
                          clanLookup: String => Option[String],
                          countryLookup: String => Option[(String, String)]) extends LookingUp {
  override def lookupDemo(gameNode: GameNode): Option[String] = demoLookup(gameNode)

  override def lookupClan(nickname: String): Option[String] = clanLookup(nickname)

  override def lookupCountryCodeAndName(ip: String): Option[(String, String)] = countryLookup(ip)
}

object BasicLookingUp {
  def empty: BasicLookingUp = BasicLookingUp(_ => None, _ => None, _ => None)

  def onlyClans: BasicLookingUp = empty.copy(
    clanLookup = name => ClanResource.clans.find(tag => name.contains(tag)).map(_.name)
  )
}
