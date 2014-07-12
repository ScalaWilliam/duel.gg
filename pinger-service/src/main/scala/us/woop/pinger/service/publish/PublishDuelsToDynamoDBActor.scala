package us.woop.pinger.service.publish

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.nio.ByteBuffer
import java.util.UUID
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, Props}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import us.woop.pinger.MyId
import us.woop.pinger.analytics.processing.DuelMaker.{CompletedDuel, GameHeader}
import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.{DynamoDBAccess, FailedToPersist}
import scala.collection.JavaConverters._
import scala.collection.immutable.Queue
import scala.concurrent.Future

class PublishDuelsToDynamoDBActor(access: DynamoDBAccess) extends Act with ActorLogging {


  whenStarting {
    // perform a test - if it fails, then we got a problem!
    // will automatically fail the actor initialisation
    access.client.putItem(access.tableName, Map(
      "GameID" -> new AttributeValue().withS(PublishDuelsToDynamoDBActor.randomUUID)
    ).asJava)
  }

  case object Process
  case class Push(completedDuel: CompletedDuel)
  case class Persisted(message: CompletedDuel)

  def queued(messages: Queue[CompletedDuel]): Receive = {
    case completedDuel: CompletedDuel if messages.isEmpty =>
      self ! Push(completedDuel)
    case Push(completedDuel) =>
      import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global
      Future(completedDuel).map(access.pushDuel).
        map(item => Persisted(completedDuel)).
        recover{case x => FailedToPersist(completedDuel, x)}.
        pipeTo(self)
    case m: Persisted =>
      context.parent ! m
//    case FailedToPersist(message, cause: ProvisionedThroughputExceededException) =>
//      become(queued(messages.enqueue(message)))
    case FailedToPersist(message, cause) =>
      log.error(cause, "Failed to persist {}: {} - retrying.", message, cause)
      become(queued(messages.enqueue(message)))
  }

  become(queued(Queue.empty))

}
object PublishDuelsToDynamoDBActor {

  case class FailedToPersist(message: CompletedDuel, dueTo: Throwable)

  def serialize(obj: Any): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    try {
      oos.writeObject(obj)
      baos.toByteArray
    } finally {
      oos.close()
      baos.close()
    }
  }

  case class DynamoDBAccess(region: Regions, accessKeyId: String, secretAccessKey: String, tableName: String, myId: MyId) {
    lazy val credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey)
    lazy val client = new AmazonDynamoDBClient(credentials) {
      setRegion(Region.getRegion(region))
    }
    def pushDuel(completedDuel: CompletedDuel) = {
      val bytes = serialize(completedDuel)
      val data = Map(
        "ClientID" -> new AttributeValue(myId.myId),
        "GameID" -> new AttributeValue(s"${completedDuel.gameHeader.startTimeText} $randomUUID"),
        "Data" -> new AttributeValue().withB(ByteBuffer.wrap(bytes)),
        "Server" -> new AttributeValue(completedDuel.gameHeader.server)
      ) ++ (if (completedDuel.gameHeader.server == GameHeader.testServer) Map("Test" -> new AttributeValue("true")) else Map.empty)
      client.putItem(tableName, data.asJava)
    }
  }

  def randomUUID = UUID.randomUUID().toString

  def props(access: DynamoDBAccess) =
    Props(classOf[PublishDuelsToDynamoDBActor], access)

}
