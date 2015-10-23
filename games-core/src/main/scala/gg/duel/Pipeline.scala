package gg.duel

import java.time.ZonedDateTime

import gcc.enrichment.{PlayerLookup, Enricher}
import org.joda.time.DateTime
import play.api.libs.json.Json

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

  val playerLookup = new PlayerLookup {
    override def lookupUserId(nickname: String, atTime: DateTime): String = "lel"
    override def lookupClanId(nickname: String, atTime: DateTime): String = "woop"
  }

  val enricher = new Enricher(playerLookup)
  val sampleJsonGame = """{"map":"ot","server":"46.101.249.112:20000","endTimeText":"2015-10-04T13:04:05Z","playedAt":[1,2,3,4,5,6,7,8,9,10],"startTimeText":"2015-10-04T12:54:05Z","players":{"w00p|lagout":{"name":"w00p|lagout","ip":"78.248.88.x","frags":88,"weapon":"grenade launcher","accuracy":30,"fragLog":[9,16,25,35,42,54,60,69,79,88],"countryName":"France","countryCode":"FR"},"Friteq":{"name":"Friteq","ip":"82.160.157.x","frags":79,"weapon":"grenade launcher","accuracy":27,"fragLog":[8,17,25,33,41,50,55,62,72,79],"countryName":"Poland","countryCode":"PL"}},"mode":"efficiency","winner":"w00p|lagout","duration":10,"type":"duel"}"""
  
  val enrichedStr = enricher.enrichJsonGame(sampleJsonGame)
  println(enrichedStr)
  import gg.duel.indexing._
  val theGame = Json.fromJson[OurGame](Json.parse(enrichedStr))
  println(theGame)
}