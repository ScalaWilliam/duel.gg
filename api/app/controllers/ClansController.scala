package controllers

import services.ClansService
import ClansService.Clan
import play.api.libs.json.Json
import play.api.mvc._
import javax.inject._

import services.ClansService

@Singleton
class ClansController @Inject()(clansService: ClansService) extends Controller {
  implicit val clansWrites = Json.writes[Clan]
  def getClans = Action {
    Ok(Json.toJson(clansService.clans))
  }
}
