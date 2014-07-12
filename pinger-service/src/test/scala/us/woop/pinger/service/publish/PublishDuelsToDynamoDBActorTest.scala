package us.woop.pinger.service.publish

import com.amazonaws.regions.Regions
import org.scalatest.{Matchers, FunSuiteLike}
import us.woop.pinger.{MyId, AmazonCredentials}
import us.woop.pinger.analytics.processing.DuelMaker.{PlayerStatistics, PlayerId, GameHeader, CompletedDuel}
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.DynamoDBAccess

class PublishDuelsToDynamoDBActorTest extends FunSuiteLike with Matchers with AmazonCredentials  {
  ignore("That publish of duel does not fail"){
    val access = DynamoDBAccess(
      accessKeyId = accessKeyId,
      secretAccessKey = secretAccessKey,
      tableName = "DuelGGDuels",
      region = Regions.EU_WEST_1,
      myId = MyId.default
    )

    val result = access.pushDuel(CompletedDuel.test)

    info(s"$result")

  }
}
