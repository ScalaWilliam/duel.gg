package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.ExecutionContext

/**
 * Created on 13/08/2015.
 */
class Main @Inject()()(implicit executionContext: ExecutionContext) extends Controller {

  def listUsers = TODO

}


