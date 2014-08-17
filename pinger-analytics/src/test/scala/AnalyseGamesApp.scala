import java.io.{File, FileInputStream}

import akka.actor.ActorSystem
import us.woop.pinger.analytics.MultiplexedDuelReader
import us.woop.pinger.data.journal.{IterationMetaData, SauerBytesWriter}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object AnalyseGamesApp extends App {
  // http://wiki.apache.org/couchdb/HTTP_Document_API#PUT
  val games =
    """
  sb-20140806-1751-0e416bc4
    sb-20140712-1458-4acb7e5f
    sb-20140806-1751-0e416bc4
    sb-20140623-1435-64aaeb38
    sb-20140712-1458-4acb7e5f
    sb-20140623-1435-64aaeb38
    sb-20140604-0524-2c174936
    sb-20140518-0007-0e591280
  sb-20140604-0524-2c174936
    sb-20140503-1800-21ab1036
    sb-20140518-0007-0e591280
  sb-20140503-1800-21ab1036
    sb-20140419-2000-06adc551
    sb-20140419-2000-06adc551
""".split("\\s+").toSet.filterNot(_.isEmpty).toList

  def gameProcessor(game: String) = {
    val is = new FileInputStream(s"$game.log")
    val readIs = SauerBytesWriter.inputStreamNumBytes(is)
    def reader = SauerBytesWriter.readSauerBytes(readIs)
    val list = Iterator.continually(reader).takeWhile(_.isDefined).map(_.get)
    MultiplexedDuelReader.multiplexSecond(list)
  }

  import scalax.io.JavaConverters._

  val metaDatas = TrieMap.empty[String, IterationMetaData].withDefault {
    id =>
      IterationMetaData.fromJson(new File(s"$id.json").asInput.string)
  }


  import spray.http._
  import spray.client.pipelining._

  implicit val system = ActorSystem()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  val completions = for {
    groupId <- games.par
    metaData = metaDatas(groupId)
    processor = gameProcessor(groupId)
  } yield for {
      completedDuel <- processor
      simpleDuel = completedDuel.toSimpleCompletedDuel
      jsonString = {
        import org.json4s._
        import org.json4s.native.Serialization
        import org.json4s.native.Serialization._
        import org.json4s.JsonDSL._
        import org.json4s.native.JsonMethods._
        implicit val formats = Serialization.formats(NoTypeHints)
        val jsonObject = parse(simpleDuel.toJson).asInstanceOf[JObject] ~ ("meta-id" -> metaData.id: JObject)
        writePretty(jsonObject)
      }
      responseF = pipeline(Put(s"http://127.0.0.1:5984/duels/${simpleDuel.simpleId}", jsonString))
    } yield for {
        response <- responseF
      } yield response.status.intValue

  // http://spray.io/documentation/1.2.1/spray-client/

  val startTime = System.currentTimeMillis()
  val responseCodes = completions.toList.flatMap(_.toList)
  val codes = Future.sequence(responseCodes).map(_.groupBy(identity).mapValues(_.size))
  codes onComplete {
    case n =>
      val endTime = System.currentTimeMillis()
      import concurrent.duration._
      val duration = (endTime - startTime).millis
      println(s"Took $duration to process the result: $n")
  }

  //  val couchDbClient = new CouchDbClient(new CouchDbProperties(
//    "duels", true, "http", "localhost", 5984, "admin", "admin"
//  ))
//
//  couchDbClient.save()



}

