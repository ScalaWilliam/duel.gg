package us.woop.pinger.service.publish

import java.io.ByteArrayInputStream
import java.security.MessageDigest

import akka.actor.ActorDSL._
import akka.actor.Props
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.{Base64, CodecUtils}
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor.{PublishedData, PushData, FailedToPush, S3Access}

import scala.concurrent.Future
import akka.pattern

import akka.pattern.pipe
import concurrent.duration._
class PublishRawMessagesToS3Actor(s3Access: S3Access, splitAt: Int) extends Act {
  whenStarting {
    s3Access.pushParsedMessages(Vector.empty)
  }
  var currentStack = Vector.empty[ParsedMessage]
  become {
    case m: ParsedMessage =>
      currentStack = currentStack :+ m
      if ( currentStack.size == 5 ) {
        self ! PushData(currentStack)
        currentStack = Vector.empty
      }
    case PushData(data) =>
      import concurrent.ExecutionContext.Implicits.global
      Future(data).map(s3Access.pushParsedMessages).
        map { x => PublishedData(x.getVersionId) }.
        recover { case failure => FailedToPush(data, failure) }.
        pipeTo(self)
    case m: PublishedData =>
      context.parent ! m
    case f @ FailedToPush(data, _) =>
      import concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(5.seconds, self, PushData(data))
      context.parent ! f
  }
  whenStopping {
    s3Access.pushParsedMessages(currentStack)
    currentStack = Vector.empty
    context.parent ! PublishedData(null)
  }
}

object PublishRawMessagesToS3Actor {
  
  import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.serialize

  case class PublishedData(versionId: String)

  case class PushData(data: Vector[ParsedMessage])

  case class FailedToPush(data: Vector[ParsedMessage], cause: Throwable)

  case class S3Access(region: Regions, accessKeyId: String, secretAccessKey: String, bucketName: String) {
    lazy val credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey)
    lazy val client = new AmazonS3Client(credentials) {
      setRegion(Region.getRegion(region))
    }
    def pushParsedMessages(parsedMessages: Vector[ParsedMessage]) = {
      val bytes = serialize(parsedMessages)
      val inputStream = new ByteArrayInputStream(bytes)
      val key = s"${System.currentTimeMillis}-${PublishDuelsToDynamoDBActor.randomUUID}"
      val md5 = CodecUtils.toStringDirect(Base64.encode(MessageDigest.getInstance("MD5").digest(bytes)))
      val metadata = new ObjectMetadata(){setContentLength(bytes.size); setContentMD5(md5) }
      client.putObject(bucketName, key, inputStream, metadata)
    }
  }

  def props(access: S3Access, splitAt: Int) =
    Props(classOf[PublishRawMessagesToS3Actor], access, splitAt )

}