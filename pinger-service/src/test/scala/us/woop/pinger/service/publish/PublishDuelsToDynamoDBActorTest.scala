package us.woop.pinger.service.publish

import com.amazonaws.regions.Regions
import org.scalatest.{Matchers, FunSuiteLike}
import us.woop.pinger.AmazonCredentials
import us.woop.pinger.analytics.processing.DuelMaker.{GameHeader, CompletedDuel}
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.service.publish.PublishDuelsToDynamoDBActor.DynamoDBAccess

class PublishDuelsToDynamoDBActorTest extends FunSuiteLike with Matchers with AmazonCredentials  {
  test("That publish of duel does not fail"){
    val access = DynamoDBAccess(
      accessKeyId = accessKeyId,
      secretAccessKey = secretAccessKey,
      tableName = "DuelGGDuels",
      region = Regions.EU_WEST_1
    )

    val result = access.pushDuel(CompletedDuel(
      gameHeader = GameHeader(
        startTime = System.currentTimeMillis,
        startMessage =  ConvertedServerInfoReply(2, 2, 2, 2, 2, false, 2, "yes", "testServer"),
        server = "TEST:1234",
        mode = "test",
        map = "test"
      ),
      nextMessage =  None,
      winner = None,
      playerStatistics = Map.empty,
      playedAt = Set.empty,
      duration = 5
    ))

    info(s"$result")

  }
}
