package gg.duel.enricher.lookup

import gg.duel.enricher.GameNode

/**
  * Created by William on 08/11/2015.
  */
object LookingUp {
  def mock = BasicLookingUp(
    demoLookup = _ => Option("abc"),
    clanLookup = _ => Option("woop"),
    countryLookup = _ => Option("GB" -> "United Kingdom")
  )
}

trait LookingUp {
  def lookupDemo(gameNode: GameNode): Option[String]
  def lookupClan(nickname: String): Option[String]
  def lookupCountryCodeAndName(ip: String): Option[(String, String)]
}

