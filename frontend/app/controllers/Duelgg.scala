package controllers

import com.xqj2.XQConnection2
import net.xqj.basex.local.BaseXConnectionPoolXQDataSource
import play.api.mvc._
import us.BaseXPersister.PublicDuelId

object Duelgg extends Controller {
  val pool = new BaseXConnectionPoolXQDataSource().getPooledConnection
  def basex = {
    val conn = pool.getConnection.asInstanceOf[XQConnection2]
//    val conn =  new net.xqj.basex.local.BaseXXQDataSource().getConnection.asInstanceOf[XQConnection2]
    val dbName = "duelsz"
    val xqe = conn.createExpression()
    xqe.executeCommand(s"OPEN $dbName")
    val sda = new us.SimpleBaseXPerister(dbName, conn, "lozlzozlolozol")

    sda
  }
  def index = Action {
    request =>
      Ok(views.html.index(basex.listDuels))
  }
  def showPage(id: String) = Action {
    request =>
      val duelId = PublicDuelId(id)
      basex.getDuel(duelId) match {
        case Some(duel) => Ok(views.html.duel(duel))
        case None => NotFound
      }
  }
}