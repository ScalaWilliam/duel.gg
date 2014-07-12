package us.woop.pinger.service.publish

import akka.actor.{PoisonPill, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.amazonaws.regions.Regions
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSuiteLike}
import us.woop.pinger.analytics.processing.DuelMaker.CompletedDuel
import us.woop.pinger.service.publish.PublishDuelsToS3Actor.{Persisted, StackedDuels, PushStackedDuels, S3Access}
import us.woop.pinger.{MyId, AmazonCredentials, ParentedProbe}


class PublishDuelsToS3ActorTest(sys: ActorSystem) extends TestKit(sys) with FunSuiteLike with Matchers with ImplicitSender with BeforeAndAfterAll with ParentedProbe with AmazonCredentials {

  def this() = this(ActorSystem())

  val access = S3Access(
    accessKeyId = accessKeyId,
    secretAccessKey = secretAccessKey,
    bucketName = "duelgg-data",
    region = Regions.EU_WEST_1,
    myId = MyId.default
  )

  ignore("That parsed message push does not fail"){
    val result = PushStackedDuels(
      stackedDuels = StackedDuels(myId = MyId.default, 1, "test", Vector(CompletedDuel.test, CompletedDuel.test))
    ).pushThrough(access)
    info(s"$result")
  }

  ignore("That data of 5-s is pushed out") {
    val chunkSize = 5
    val s3Actor = parentedProbe(PublishDuelsToS3Actor.props(chunkSize, access))
    def sendMessage(): Unit = {
      s3Actor ! CompletedDuel.test
    }
    1 until chunkSize foreach { _ => sendMessage() }
    expectNoMsg()
    sendMessage()
    expectMsgClass(classOf[Persisted])
  }

  ignore("That data is pushed out at stop") {
    val chunkSize = 5
    val s3Actor = parentedProbe(PublishDuelsToS3Actor.props(chunkSize, access))
    s3Actor ! CompletedDuel.test
    s3Actor ! PoisonPill
    expectMsgClass(classOf[Persisted])
    expectNoMsg()
  }

}

