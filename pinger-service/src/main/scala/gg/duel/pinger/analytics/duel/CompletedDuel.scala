package gg.duel.pinger.analytics.duel

import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply

case class CompletedDuel(gameHeader: GameHeader, nextMessage: Option[ConvertedServerInfoReply],
                         winner: Option[(PlayerId, PlayerStatistics)],
                         playerStatistics: Map[PlayerId, PlayerStatistics],
                         playedAt: Set[Int], duration: Int, metaId: Option[String] = None){
  override def toString =
    s"""CompletedDuel(gameHeader = $gameHeader, winner = $winner, playerStatistics = $playerStatistics, duration = $duration, playedAt = $playedAt, nextMessage = $nextMessage)"""

  def toSimpleCompletedDuel = {
    SimpleCompletedDuel(
      simpleId= s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
      duration = duration,
      playedAt = playedAt.toSet.toList.sorted,
      startTimeText = gameHeader.startTimeText,
      startTime = gameHeader.startTime,
      map = gameHeader.map,
      mode = gameHeader.mode,
      server = gameHeader.server,
      players =
        for {(playerId, playerStats) <- playerStatistics}
        yield playerId.name -> SimplePlayerStatistics(
          name = playerId.name,
          ip = playerId.ip,
          accuracy = playerStats.accuracy,
          frags = playerStats.frags,
          weapon = playerStats.weapon,
          fragLog = playerStats.fragsLog.toList
        ),
      winner = winner.map(_._1.name),
      metaId = metaId
    )
  }
}

object CompletedDuel {
  def test = CompletedDuel(
    gameHeader = GameHeader(
      startTime = System.currentTimeMillis,
      startMessage = ConvertedServerInfoReply(2, 2, 2, 2, 2, false, 2, "yes", "testServer"),
      server = "TEST:1234",
      mode = "test",
      map = "test"
    ),
    nextMessage = None,
    winner = Option(PlayerId("Test", "a.b.c.x") -> PlayerStatistics(frags = 5, accuracy = 35, fragsLog = Map(1 -> 2), weapon = "test")),
    playerStatistics = Map(
      PlayerId("Test", "a.b.c.x") -> PlayerStatistics(frags = 5, fragsLog = Map(1 -> 2), accuracy = 35, weapon = "test"),
      PlayerId("Best", "a.b.c.x") -> PlayerStatistics(frags = 20, fragsLog = Map(1 -> 2, 3-> 4), accuracy = 91, weapon = "rocket launcher")
    ),
    playedAt = Set(1,2,5),
    duration = 5
  )

}