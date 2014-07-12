package us.woop.pinger.service.publish

import java.io.ByteArrayInputStream
import java.security.MessageDigest

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, Props}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.{Base64, CodecUtils}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import us.woop.pinger.{SystemConfiguration, MyId}
import us.woop.pinger.analytics.processing.DuelMaker.CompletedDuel
import us.woop.pinger.service.publish.PublishDuelsToS3Actor._

import scala.concurrent.Future

class PublishDuelsToS3Actor(chunkSize: Int, s3Access: S3Access, myId: MyId) extends Act with ActorLogging{

  def blankStack = StackedDuels(myId, 10, ISODateTimeFormat.dateTime().print(new DateTime), Vector.empty)

  var stackedDuel = blankStack

  whenStarting {
    PushStackedDuels(blankStack).pushThrough(s3Access)
  }

  import akka.pattern.pipe

import scala.concurrent.duration._
  become {
    case Flush if stackedDuel.duels.nonEmpty =>
      self ! PushStackedDuels(stackedDuel)
      stackedDuel = blankStack
    case pushMe: PushStackedDuels =>
      import scala.concurrent.ExecutionContext.Implicits.global
      Future(pushMe).map(_.pushThrough(s3Access)).map(_ => Persisted(pushMe)).recover{case cause => FailedPush(pushMe, cause)} pipeTo self
    case failedPush: FailedPush =>
      import scala.concurrent.ExecutionContext.Implicits.global
      log.error(failedPush.cause, "Failed to push {}", failedPush.pushStackDuels)
      context.system.scheduler.scheduleOnce(
        delay = 5.seconds,
        receiver = self,
        message = failedPush.copy(
          pushStackDuels = failedPush.pushStackDuels.copy(
            attempt = failedPush.pushStackDuels.attempt + 1
          )
        )
      )
    case m: Persisted =>
      context.parent ! m
    case cd: CompletedDuel =>
      stackedDuel = stackedDuel.copy(duels = stackedDuel.duels :+ cd)
      if ( stackedDuel.duels.size == chunkSize ) {
        self ! PushStackedDuels(stackedDuel)
        stackedDuel = blankStack
      }
  }

  whenStopping {
    if ( stackedDuel.duels.nonEmpty ) {
      PushStackedDuels(stackedDuel).pushThrough(s3Access)
    }
  }

}
object PublishDuelsToS3Actor {

  case class Persisted(pushStackDuels: PushStackedDuels)

  def props(chunkBy: Int, s3Access: S3Access, myId: MyId = MyId.default) =
    Props(classOf[PublishDuelsToS3Actor], chunkBy, s3Access, myId)

  case class FailedPush(pushStackDuels: PushStackedDuels, cause: Throwable)

  case class PushStackedDuels(stackedDuels: StackedDuels, attempt: Int = 0) {
    val key = s"${stackedDuels.myId}/${System.currentTimeMillis}${stackedDuels.id}.dat"
    def pushThrough(s3Access: S3Access) =
      s3Access.pushObject(key, stackedDuels)
  }

  case object Flush

  case class StackedDuels(myId: MyId, id: Int, dateTime: String, duels: Vector[CompletedDuel])

  object S3Access {
    def apply(myId: MyId):S3Access = S3Access(
      region = Regions.EU_WEST_1,
      accessKeyId = SystemConfiguration.accessKeyId,
      secretAccessKey = SystemConfiguration.secretAccessKey,
      bucketName = SystemConfiguration.bucketName,
      myId
    )
  }

  case class S3Access(region: Regions, accessKeyId: String, secretAccessKey: String, bucketName: String, myId: MyId) {
    lazy val credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey)
    lazy val client = new AmazonS3Client(credentials) {
      setRegion(Region.getRegion(region))
    }

    def pushObject(key: String, stackedDuels: StackedDuels) = {
      pushBytes(key, bytes = {
        val objects = Map(
//          'id -> stackedDuels.id,
//          'myId -> stackedDuels.myId,
//          'dateTime -> stackedDuels.dateTime,
//          'duels -> (for { duel <- stackedDuels.duels } yield Map(
//            'duration -> duel.duration,
//            'startTimeText -> duel.gameHeader.startTimeText,
//            'startTime -> duel.gameHeader.startTime,
//            'map -> duel.gameHeader.map,
//            'mode -> duel.gameHeader.mode,
//            'server -> duel.gameHeader.server,
//            'winner -> duel.winner.map(winner =>
//              Map(
//                'ip -> winner._1.ip,
//                'name -> winner._1.name,
//                'frags -> winner._2.frags,
//              duel.winner.,
//
//          ))
        )
        import org.json4s._
        import org.json4s.native.Serialization
        import org.json4s.native.Serialization.write
        implicit val formats = Serialization.formats(NoTypeHints)
        write(objects)
      }.getBytes("UTF-8"))
    }
    def pushBytes(key: String, bytes: Array[Byte]) = {
      val inputStream = new ByteArrayInputStream(bytes)
      val md5 = CodecUtils.toStringDirect(Base64.encode(MessageDigest.getInstance("MD5").digest(bytes)))
      val metadata = new ObjectMetadata(){setContentLength(bytes.size); setContentMD5(md5) }
      client.putObject(bucketName, key, inputStream, metadata)
    }
  }
}