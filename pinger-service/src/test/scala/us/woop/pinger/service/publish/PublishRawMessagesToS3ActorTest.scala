package us.woop.pinger.service.publish

import akka.actor.{PoisonPill, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import com.amazonaws.regions.Regions
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.data.Stuff.{Server, IP}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.{MyId, AmazonCredentials, ParentedProbe}
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor.{PublishedData, S3Access}

class PublishRawMessagesToS3ActorTest(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe with AmazonCredentials {

  def this() = this(ActorSystem())

  val access = S3Access(
    accessKeyId = accessKeyId,
    secretAccessKey = secretAccessKey,
    bucketName = "duelgg-data",
    region = Regions.EU_WEST_1,
    myId = MyId.default
  )

  test("That parsed message push does not fail"){
    val result = access.pushParsedMessages(Vector.empty)
    info(s"$result")
  }

  test("That data of 5-s is pushed out") {
    val splitAt = 5
    val s3Actor = parentedProbe(PublishRawMessagesToS3Actor.props(access, splitAt))
    def sendMessage(): Unit = {
      s3Actor ! ReceivedBytes(Server(IP("127.0.0.1"), 1234), System.currentTimeMillis, ByteString(1,2,3))
    }
    1 until splitAt foreach { _ => sendMessage() }
    expectNoMsg()
    sendMessage()
    expectMsgClass(classOf[PublishedData])
  }

  test("That data is pushed out at stop") {
    val s3Actor = parentedProbe(PublishRawMessagesToS3Actor.props(access, 5))

    def sendMessage(): Unit = {
      s3Actor ! ReceivedBytes(Server(IP("127.0.0.1"), 1234), System.currentTimeMillis, ByteString(1,2,3))
    }
    sendMessage()
    expectNoMsg()
    s3Actor ! PoisonPill
    expectMsgClass(classOf[PublishedData])
    expectNoMsg()

  }

}
