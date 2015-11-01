package modules

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, LocalDateTime, ZonedDateTime}
import javax.inject._

import modules.OgroDemoParser.Demo
import org.jsoup.Jsoup
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OgroDemoParser @Inject()()(implicit wSClient: WSClient, executionContext: ExecutionContext) {
  def getDemosF(serverId: String): Future[List[Demo]] = {
    Logger.info(s"Loading demos for server ID $serverId")
    wSClient.url("http://ogros.org/server/demos.php").post(
      Map("results" -> Seq.empty[String],
        "timezone" -> Seq("5"), // odd, ogro times don't match our times by 5 hours. Very odd.
        "server" -> Seq(serverId))
    ).map(_.body).map(OgroDemoParser.parsePage)
  }
}
object OgroDemoParser {

  val servers = Map(
    "46.101.249.112 10000" -> "effic.me 1",
    "46.101.249.112 20000" -> "effic.me 2",
    "46.101.249.112 30000" -> "effic.me 3",
    "46.101.249.112 40000" -> "effic.me 4",
    "46.101.249.112 50000" -> "effic.me 5",
    "46.101.249.112 60000" -> "effic.me 6",
    "46.101.152.151 10000" -> "ogros.org 1",
    "46.101.152.151 20000" -> "ogros.org 2",
    "46.101.152.151 30000" -> "ogros.org 3"
  )

  object ServerName {
    def unapply(server: String): Option[String] = servers.get(server)
  }

  case class Demo(dateTime: ZonedDateTime, mode: String, map: String, url: String)

  def parsePage(contents: String): List[Demo] = {
    val document = Jsoup.parse(contents)
    import scala.collection.JavaConverters._
    for {
      tr <- document.select("tr").asScala.toList
      dateTime <- tr.select("td:nth-child(1)").asScala.flatMap(x => Option(x.text).filter(_.nonEmpty))
      mode <- tr.select("td:nth-child(2)").asScala.flatMap(x => Option(x.text).filter(_.nonEmpty))
      map <- tr.select("td:nth-child(3)").asScala.flatMap(x => Option(x.text).filter(_.nonEmpty))
      link <- tr.select("a").asScala.flatMap(x => Option(x.attr("href")).filter(_.nonEmpty))
    } yield Demo(
      dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("UTC")),
      mode = mode,
      map = map,
      url = link
    )
  }

}
