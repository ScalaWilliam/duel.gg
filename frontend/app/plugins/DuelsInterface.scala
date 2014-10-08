package plugins

import java.net.URL
import play.api._
import play.api.libs.ws._
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal
import scala.xml.Elem
import scalax.io.JavaConverters._
class DuelsInterface(implicit app: Application) extends Plugin {

  override def enabled = true

  lazy val url: String = {
    val path = "plugins.DuelsInterface.restPath"
    app.configuration.getString(path) match {
      case None =>
        throw app.configuration.reportError(path, s"Required a value at $path")
      case Some(uri) if Try(new URL(uri)).isFailure =>
        throw app.configuration.reportError(path, s"Required value at $path is not a valid URL: $uri")
      case Some(validUri) => validUri
    }
  }

  val dbName: String = {
    val dbNamePath = "plugins.DuelsInterface.dbPath"
    app.configuration.getString(dbNamePath).getOrElse{
      throw app.configuration.reportError(dbNamePath, s"Required value at $dbNamePath is not set.")
    }
  }


  
  def holder = {
    WS.url(url)
      .withHeaders("Accept" -> "application/xml")
      .withRequestTimeout(9000)
  }

  val getPlayerProfileXq = {
    app.resourceAsStream("/get-player-profile.xq").get.asInput.string
  }
  val getDuelXq = {
    app.resourceAsStream("/get-duel.xq").get.asInput.string
  }

  val getIndexXq = {
    app.resourceAsStream("/get-index-with-groups.xq").get.asInput.string
  }
  val getIndexDuelXq = {
    app.resourceAsStream("/get-index-duel.xq").get.asInput.string
  }

  def getIndex = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      index <- holder.post(
        <query xmlns='http://basex.org/rest'><text>{getIndexXq}</text>
      </query>)
      xmlData = try {
        index.xml
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed to parse due to:\n$e\n${index.body.take(300)}", e)
      }
    } yield xmlData

  }

  def getIndexDuel(duelId: String): Future[Option[Elem]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      index <- holder.post(
        <query xmlns='http://basex.org/rest'><text>{getIndexDuelXq}</text>
      <variable name="web-id" value={duelId}/>
      </query>)
      xmlDataO = if ( index.body.isEmpty) None else Option(try {
        index.xml
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed to parse due to:\n$e\n${index.body.take(300)}", e)
      })
    } yield xmlDataO

  }
  def getPlayer(playerName: String): Future[Option[Elem]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      index <- holder.post(
        <query xmlns='http://basex.org/rest'><text>{getPlayerProfileXq}</text>
      <variable name="player-name" value={playerName}/>
      </query>)
      xmlDataO = if ( index.body.isEmpty) None else Option(try {
        index.xml
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed to parse due to:\n$e\n${index.body.take(300)}", e)
      })
    } yield xmlDataO

  }

  def getDuel(duelId: String): Future[Option[Elem]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      duel <- holder.post(<query xmlns="http://basex.org/rest">
        <text>
          {getDuelXq}
        </text>
        <variable name="web-id" value={duelId}/>
      </query>
      )
    } yield if ( duel.body.isEmpty ) None else Option(try {
        duel.xml
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed to parse ${duel.body}", e)
      })
  }

}
object DuelsInterface {
  def duelsInterface: DuelsInterface = Play.current.plugin[DuelsInterface]
    .getOrElse(throw new RuntimeException("DuelsInterface plugin not loaded"))
}
