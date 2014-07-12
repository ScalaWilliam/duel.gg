package us.woop.pinger.service.publish

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import us.woop.pinger.data.Stuff.{IP, Server}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.publish.PublishRawMessagesToS3Actor.{PublishedData, S3Access}
import us.woop.pinger.{MyId, ParentedProbe}

class PublishRawMessagesToS3ActorTest(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe  {

  def this() = this(ActorSystem())

  val access = S3Access(
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
