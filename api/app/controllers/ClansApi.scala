package controllers

import modules.ClansService
import modules.ClansService.Clan
import play.api.libs.json.Json
import play.api.mvc._
import javax.inject._

@Singleton
class ClansApi @Inject()(clansService: ClansService) extends Controller {
  implicit val clansWrites = Json.writes[Clan]
  def getClans = Action {
    Ok(Json.toJson(clansService.clans))
  }
}
