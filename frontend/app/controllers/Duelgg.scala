package controllers

import play.api.Play
import play.api.libs.json.{JsValue, JsObject}
import play.api.mvc._
import play.twirl.api.Html
import plugins.{DuelsStream, DuelsInterface}

object Duelgg extends Controller {


  def index = Action.async {
    request =>
      import scala.concurrent.ExecutionContext.Implicits.global
      for {
        xmlData <- DuelsInterface.duelsInterface.getIndex
      } yield Ok(views.html.index(Html(s"$xmlData")))
  }

  def showPage(id: String) = Action.async {
    request =>
      import scala.concurrent.ExecutionContext.Implicits.global
      val resultO = for {
        xmlO <- DuelsInterface.duelsInterface.getDuel(id)
      } yield for {
        xml <- xmlO
        title = (xml \ "title").text
        article = xml \ "article"
      } yield Ok(views.html.duel(title)(Html(s"$article")))

      resultO.map(_.getOrElse(NotFound))
  }

  def streamSocket = {
    import Play.current
    WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
      DuelsStream.duelsStream.createListenerActor(out)
    }
  }

}