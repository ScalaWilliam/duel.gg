package gg.duel

import java.time.ZonedDateTime

import gcc.enrichment.{GetsPlayerInfo, GameEnricherImpl, GameEnricher, ProvidesCountryImpl}
import org.joda.time.DateTime

object Pipeline extends App {
  /**
   *
   * The pipeline is built in two key stages:
   *
   * Enrichment:
   * 1. Attach user's country code and country name
   * 2. Attach user's nickname
   * 3. Attach user's ID
   * 4. Attach user's clan
   * 5. If CTF, determine whether it's a clanmatch - if so, attach clan IDs to teams.
   * 6. Attach end time of the game
   * 7. Tidy up fraglog
   *
   * Indexing:
   * 1. Extract game type
   * 2. Extract game time
   * 3. Extract player names
   * 4. Extract player clans
   *
   */

  val q: GameEnricher = new GameEnricherImpl
  val sampleGame = """{"map":"ot","server":"46.101.249.112:20000","endTimeText":"2015-10-04T13:04:05Z","playedAt":[1,2,3,4,5,6,7,8,9,10],"startTimeText":"2015-10-04T12:54:05Z","players":{"w00p|lagout":{"name":"w00p|lagout","ip":"78.248.88.x","frags":88,"weapon":"grenade launcher","accuracy":30,"fragLog":[9,16,25,35,42,54,60,69,79,88],"countryName":"France","countryCode":"FR"},"Friteq":{"name":"Friteq","ip":"82.160.157.x","frags":79,"weapon":"grenade launcher","accuracy":27,"fragLog":[8,17,25,33,41,50,55,62,72,79],"countryName":"Poland","countryCode":"PL"}},"mode":"efficiency","winner":"w00p|lagout","duration":10,"type":"duel"}"""
  val pc = new ProvidesCountryImpl

  val gpi = new GetsPlayerInfo {
    override def getClan(s: String, zonedDateTime: DateTime, s1: String): String = if ( s.startsWith("w00p|") ) "woop" else null
    override def getUsername(s: String, zonedDateTime: DateTime, s1: String): String = if ( "w00p|lagout" == s ) "lagout" else null
  }

  println(q.toJson(q.fullyEnriched(q.parseGame(sampleGame), pc, gpi)))

}