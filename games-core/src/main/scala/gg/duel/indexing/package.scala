package gg.duel

import play.api.libs.json.Json

/**
 * Created on 04/10/2015.
 */
package object indexing {

  implicit val ourPlayerReads = Json.reads[OurPlayer]
  implicit val ourTeamReads = Json.reads[OurTeam]
  implicit val ourGameReads = Json.reads[OurGame]
}
