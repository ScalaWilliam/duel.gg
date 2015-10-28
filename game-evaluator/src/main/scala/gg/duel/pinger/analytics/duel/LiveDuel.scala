package gg.duel.pinger.analytics.duel


/**
 * Created on 25/07/2015.
 */
case class LiveDuel
(
  simpleId: String,
  duration: Int,
  playedAt: List[Int],
  startTimeText: String,
  startTime: Long,
  secondsRemaining: Int,
  map: String,
  mode: String,
  server: String,
  players: Map[String, SimplePlayerStatistics],
  winner: Option[String], metaId: Option[String]
  ) {
  def asScd = SimpleCompletedDuel(simpleId, duration, playedAt, startTimeText, startTime, map, mode, server, players, winner, metaId)
  def toJson = asScd.toJson
  def toPrettyJson = asScd.toPrettyJson
}
object LiveDuel {
  // todo later
//  def processState(atServer: Server, sIteratorState: SIteratorState): Option[(Server, LiveDuel)] = {
//    for {
//      SProcessing(MProcessing(serverStates, _)) <- Option(sIteratorState)
//      ZInDuelState(_, td: TransitionalBetterDuel) <- serverStates.get(atServer)
//      liveDuel <- td.liveDuel.toOption
//    } yield (atServer, liveDuel)
//  }

  /**
   * Clean up:  case CleanupOld =>
          val removeServersA = for {
            (server, Some(liveDuel)) <- currentDetail.toList
            // 15 minutes
            if System.currentTimeMillis - liveDuel.startTime > (15 * 60 * 1000)
          } yield server
          val removeServersB = lastUpdated.filter(k => System.currentTimeMillis() - k._2 > 10 * 1000).map(_._1)
          val removeServers = (removeServersA ++ removeServersB).toSet
          removeServers foreach currentDetail.remove
          removeServers map (_.toString) filter hazelcastStatusMap.containsKey foreach hazelcastStatusMap.remove
   */
}