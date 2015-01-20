package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.BetterDuelState.StateTransition
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.{PartialPlayerExtInfo, PlayerExtInfo, ParsedMessage}
import gg.duel.pinger.data.{ModesList, Server}
import org.scalactic._

trait BetterDuelState {
  def gameHeader: GameHeader
  def next: StateTransition
}

case class PlayerId(name: String, ip: String)

case class PlayerStatistics(frags: Int, fragsLog: Map[Int, Int], weapon: String, accuracy: Int)

case class SimplePlayerStatistics
(
  name: String,
  ip: String,
  frags: Int,
  weapon: String,
  accuracy: Int,
  fragLog: List[(Int, Int)])


case class SecondsRemaining(seconds: Int)

case class Frags(frags: Int)

case class Weapon(weapon: String)

case class Accuracy(accuracy: Int)

case class LogItem(playerId: PlayerId, remaining: SecondsRemaining, frags: Frags, weapon: Weapon, accuracy: Accuracy)

case class DuelAccumulation(playerStatistics: List[LogItem])


object BetterDuelState {

  val duelModeNames = Set("ffa", "instagib", "efficiency")

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  type StateTransition = PartialFunction[ParsedMessage, BetterDuelState Or Every[ErrorMessage]]

}

case class BetterDuelFound(gameHeader: GameHeader, simpleCompletedDuel: SimpleCompletedDuel) extends BetterDuelState {
  lazy val next: StateTransition = throw new NotImplementedError("Should not get here...")
}

case class TransitionalBetterDuel(gameHeader: GameHeader, isRunning: Boolean, timeRemaining: Int, duelAccumulation: DuelAccumulation) extends BetterDuelState {
  override def next: StateTransition = {
    case ParsedMessage(_, time, info: PlayerExtInfo) if info.state <= 3 && isRunning =>
      val logItem = LogItem(
        playerId = PlayerId(info.name, info.ip),
        remaining = SecondsRemaining(timeRemaining),
        frags = Frags(info.frags - info.teamkills),
        weapon = Weapon(ModesList.guns.getOrElse(info.gun, "unknown")),
        accuracy = Accuracy(info.accuracy)
      )
      val newAccummulation = duelAccumulation.copy(
        playerStatistics = duelAccumulation.playerStatistics :+ logItem
      )
      Good(this.copy(duelAccumulation = newAccummulation))
    // dealing with PSL Thomas bullshit
    // sends -3 followed by 7 ints, and possibly several times - overriding parts of the player info, such as name.
    // so we'll override with a valid player ID
    case ParsedMessage(s, time, PartialPlayerExtInfo(ino)) if ino.state <= 3 && isRunning =>
      duelAccumulation.playerStatistics.find(ps => ps.playerId.ip == ino.ip && ps.playerId.name.endsWith(ino.name)) match {
        case Some(playerAccummulation) =>
          next.apply(ParsedMessage(s, time, ino.copy(name = playerAccummulation.playerId.name)))
        case None =>
          // we'll let it pass through, but it might cause the game to fail silently
          Good(this)
      }
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 && timeRemaining == 0 =>
      completeDuel(this)(Option(info)).map(BetterDuelFound(gameHeader, _)) match {
        case result@Bad(reasons) =>
          // psl override - in case they don't send enough data, we'll wait one more tick
          // they send thomas extinfo sometimes instead of the usual extinfo
          // and some packets go haywre as well. you guys fucked up.
          if (reasons.toList.exists(_ contains "log item to say that both players finished the game")
            && (gameHeader.startMessage.description contains "PSL")) {
            Good(this)
          } else {
            result
          }
        case other => other
      }
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 =>
      Good(copy(isRunning = true, timeRemaining = 0))
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if BetterDuelState.isSwitch(gameHeader.startMessage, info) =>
      completeDuel(this)(Option(info)).map(BetterDuelFound(gameHeader, _))
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) =>
      Good(copy(isRunning = !info.gamepaused, timeRemaining = info.remain))
    case other => Good(this)
  }

  def completeDuel(transitionalDuel: TransitionalBetterDuel)(nextMessage: Option[ConvertedServerInfoReply]): SimpleCompletedDuel Or Every[ErrorMessage] = {
    import transitionalDuel.duelAccumulation.playerStatistics
    for {
      twoPlayers <- {
        val activePlayers = playerStatistics.map(_.playerId).toSet
        activePlayers.toList match {
          case first :: second :: Nil => Good(List(first, second))
          case other => Bad(One(s"Expected exactly two players, got: $other"))
        }
      }
      startedSeconds <- {
        if (playerStatistics.isEmpty) {
          Bad(One("Player log empty"))
        } else {
          Good(playerStatistics.map(_.remaining).map(_.seconds).max)
        }
      }
      bothPlayersStarted = {
        twoPlayers.forall(playerId =>
          playerStatistics.exists(logItem =>
            logItem.remaining.seconds == startedSeconds &&
              logItem.playerId == playerId))
      }
      bothPlayersFinished = {
        twoPlayers.forall(playerId =>
          playerStatistics.exists(logItem =>
            logItem.remaining.seconds <= 3 &&
              logItem.playerId == playerId))
      }
      _ <- if (bothPlayersStarted) Good(Unit) else Bad(One("Could not find a log item to say that both players started the game"))
      durationSeconds = playerStatistics.head.remaining.seconds
      durationMinutes = Math.ceil(durationSeconds / 60.0).toInt
      foundDurationSeconds = durationSeconds - playerStatistics.last.remaining.seconds
      foundDurationMinutes = Math.ceil(foundDurationSeconds / 60.0).toInt
      _ <- if (foundDurationMinutes >= 8 ) Good(Unit) else Bad(One(s"Expected at least 8 minutes to duel, found $foundDurationMinutes (${foundDurationSeconds}s)"))

      _ <- if (bothPlayersFinished) Good(Unit) else Bad(One(s"Could not find a log item to say that both players finished the game ($nextMessage)"))
      players = {
        for {
          playerId@PlayerId(name, ip) <- twoPlayers
          hisStats = playerStatistics.filter(_.playerId == playerId)
          simplePlayerStatistics = SimplePlayerStatistics(
            name, ip, accuracy = hisStats.last.accuracy.accuracy,
            frags = hisStats.last.frags.frags,
            weapon = hisStats.groupBy(_.weapon).toList.sortBy(_._2.size).head._1.weapon,
            fragLog = {
              val first = hisStats.groupBy(stat => Math.ceil((durationSeconds - stat.remaining.seconds) / 60.0)).mapValues(_.minBy(_.remaining.seconds)).toList.sortBy(_._1)
              first.map(eh => eh._1.toInt -> eh._2.frags.frags).filterNot(_._1 == 0)
            }
          )
        } yield name -> simplePlayerStatistics
      }
      isValidEfficGame <- {
        if (transitionalDuel.gameHeader.mode == "efficiency" && players.forall(_._2.frags >= 10))
          Good(Unit)
        else if (transitionalDuel.gameHeader.mode == "efficiency")
          Bad(One(s"in efficiency both frags to be >=10, got ${players.map(_._2.frags)}"))
        else Good(Unit)
      }
      isValidInstagibGame <- {
        if (transitionalDuel.gameHeader.mode == "instagib" && players.forall(_._2.frags >= 20))
          Good(Unit)
        else if (transitionalDuel.gameHeader.mode == "instagib")
          Bad(One(s"In instagib expect both frags to be >= 20, got ${players.map(_._2.frags)}"))
        else Good(Unit)
      }
      isValidFFAGame <- {
        val sum = players.map(_._2.frags).sum
        if (transitionalDuel.gameHeader.mode == "ffa" && sum >= 15)
          Good(Unit)
        else if (transitionalDuel.gameHeader.mode == "ffa")
          Bad(One(s"In ffa expect sum of frags to be >= 15, got , got $sum"))
        else Good(Unit)
      }
      winner = if (players.map(_._2.frags).toSet.size == 1) None
      else {
        Option(players.maxBy(_._2.frags)._1)
      }
    } yield
      SimpleCompletedDuel(
        simpleId = s"${transitionalDuel.gameHeader.startTimeText}::${transitionalDuel.gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
        duration = durationMinutes,
        playedAt = playerStatistics.map(_.remaining.seconds).map(t => (t / 60) + 1).toSet.toList.sorted,
        startTimeText = transitionalDuel.gameHeader.startTimeText,
        startTime = transitionalDuel.gameHeader.startTime,
        map = transitionalDuel.gameHeader.map,
        mode = transitionalDuel.gameHeader.mode,
        server = transitionalDuel.gameHeader.server,
        players = players.toMap, winner = winner, metaId = None
      )

  }
}

