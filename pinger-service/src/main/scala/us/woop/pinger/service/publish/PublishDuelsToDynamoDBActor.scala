package us.woop.pinger.service.publish

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.nio.ByteBuffer
import java.util.UUID
import akka.actor.ActorDSL._
import akka.actor.Props
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import us.woop.pinger.analytics.processing.DuelMaker.CompletedDuel
import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.{DynamoDBAccess, FailedToPersist}
import scala.collection.JavaConverters._
import scala.concurrent.Future

class PublishDuelsToDynamoDBActor(access: DynamoDBAccess) extends Act {
  whenStarting {
    // perform a test - if it fails, then we got a problem!
    // will automatically fail the actor initialisation
    access.client.putItem(access.tableName, Map(
      "GameID" -> new AttributeValue().withS(PublishDuelsToDynamoDBActor.randomUUID)
    ).asJava)
  }
  become {
    case completedDuel: CompletedDuel =>
      import scala.concurrent.ExecutionContext.Implicits.global
      Future(completedDuel).map(access.pushDuel) onFailure {
        case x => self ! FailedToPersist(completedDuel, x)
      }
      // re-schedule if we fail. Or something like that.
  }
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

  case class DynamoDBAccess(region: Regions, accessKeyId: String, secretAccessKey: String, tableName: String) {
    lazy val credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey)
    lazy val client = new AmazonDynamoDBClient(credentials) {
      setRegion(Region.getRegion(region))
    }
    def pushDuel(completedDuel: CompletedDuel) = {
      val bytes = serialize(completedDuel)
      val data = Map(
        "GameID" -> new AttributeValue().withS(s"${completedDuel.gameHeader.startTimeText} $randomUUID"),
        "Data" -> new AttributeValue().withB(ByteBuffer.wrap(bytes)),
        "Server" -> new AttributeValue().withS(completedDuel.gameHeader.server)
      )
      client.putItem(tableName, data.asJava)
    }
  }

  def randomUUID = UUID.randomUUID().toString

  def props(access: DynamoDBAccess) =
    Props(classOf[PublishDuelsToDynamoDBActor], access)

}
