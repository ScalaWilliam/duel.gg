package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.CleanupDescription
import gg.duel.pinger.analytics.duel.BetterDuelState.StateTransition
import gg.duel.pinger.analytics.duel.DuelParseError._
import gg.duel.pinger.data.ModesList
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import gg.duel.pinger.data.ParsedPongs.{ParsedMessage, PartialPlayerExtInfo, PlayerExtInfo}
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

case class DuelAccumulation(private val playerStatistics: List[LogItem]) {
  def isEmpty = playerStatistics.isEmpty

  def view = playerStatistics.view

  def forPlayer(playerId: PlayerId) = copy(playerStatistics = view.filter(_.playerId == playerId).toList)

  def playerIds = playerStatistics.view.map(_.playerId).toSet

  def append(logItem: LogItem): DuelAccumulation = copy(playerStatistics = logItem +: playerStatistics)

  def earliest: LogItem = playerStatistics.last

  def latest: LogItem = playerStatistics.head
}

object DuelAccumulation {
  def empty = DuelAccumulation(Nil)
}


object BetterDuelState {

  val duelModeNames = Set("ffa", "instagib", "efficiency")

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  type StateTransition = PartialFunction[ParsedMessage, BetterDuelState Or Every[DuelParseError]]

}

case class BetterDuelFound(gameHeader: GameHeader, simpleCompletedDuel: SimpleCompletedDuel) extends BetterDuelState {
  lazy val next: StateTransition = throw new NotImplementedError("Should not get here...")
}

case class TransitionalBetterDuel(gameHeader: GameHeader, isRunning: Boolean, timeRemaining: SecondsRemaining, duelAccumulation: DuelAccumulation) extends BetterDuelState {
  override def next: StateTransition = {
    case ParsedMessage(_, time, info: PlayerExtInfo) if info.state <= 3 && isRunning =>
      val logItem = LogItem(
        playerId = PlayerId(info.name, info.ip),
        remaining = timeRemaining,
        frags = Frags(info.frags - info.teamkills),
        weapon = Weapon(ModesList.guns.getOrElse(info.gun, "unknown")),
        accuracy = Accuracy(info.accuracy)
      )
      val newAccummulation = duelAccumulation.append(logItem)
      Good(this.copy(duelAccumulation = newAccummulation))
    // dealing with PSL Thomas bullshit
    // sends -3 followed by 7 ints, and possibly several times - overriding parts of the player info, such as name.
    // so we'll override with a valid player ID
    case ParsedMessage(s, time, PartialPlayerExtInfo(ino)) if ino.state <= 3 && isRunning =>
      duelAccumulation.view.reverse.find(ps => ps.playerId.ip == ino.ip && ps.playerId.name.endsWith(ino.name)) match {
        case Some(logItem) =>
          next.apply(ParsedMessage(s, time, ino.copy(name = logItem.playerId.name)))
        case None =>
          // we'll let it pass through, but it might cause the game to fail silently
          Good(this)
      }
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 && timeRemaining.seconds == 0 =>
      this.completeDuel(Option(info)).map(BetterDuelFound(gameHeader, _)) match {
        case result@Bad(reasons) =>
          // psl override - in case they don't send enough data, we'll wait one more tick
          // they send thomas extinfo sometimes instead of the usual extinfo
          // and some packets go haywre as well. you guys fucked up.
          if (reasons.toList.contains(CouldNotFindProofThatThePlayersFinished)
            && (gameHeader.startMessage.description contains "PSL")) {
            Good(this)
          } else {
            result
          }
        case other => other
      }
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 =>
      Good(copy(isRunning = true, timeRemaining = SecondsRemaining(0)))
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) if BetterDuelState.isSwitch(gameHeader.startMessage, info) =>
      this.completeDuel(Option(info)).map(BetterDuelFound(gameHeader, _))
    case ParsedMessage(_, time, info: ConvertedServerInfoReply) =>
      Good(copy(isRunning = !info.gamepaused, timeRemaining = SecondsRemaining(info.remain)))
    case other => Good(this)
  }

  def twoPlayersOr = {
    duelAccumulation.playerIds.toList match {
      case first :: second :: Nil => Good(List(first, second))
      case other => Bad(One(ExpectedExactly2Players(other.map(_.name))))
    }
  }

  def startedSecondsOr = {
    if (duelAccumulation.isEmpty) {
      Bad(One(PlayerLogEmpty))
    } else {
      Good(duelAccumulation.view.map(_.remaining).map(_.seconds).max)
    }
  }

  def allPlayersStartedOr(startedSeconds: Int) = {
    val bv = duelAccumulation.playerIds.forall(playerId =>
      duelAccumulation.view.exists(logItem =>
        logItem.remaining.seconds == startedSeconds &&
          logItem.playerId == playerId))
    if (bv) Good(Unit) else Bad(One(CouldNotFindLogItemToSayAllPlayersStarted))
  }

  def allPlayersFinishedOr(nextMessage: Option[ConvertedServerInfoReply]) = {
    val bv = duelAccumulation.playerIds.forall(playerId =>
      duelAccumulation.view.exists(logItem =>
        logItem.remaining.seconds <= 3 &&
          logItem.playerId == playerId))
    if (bv) Good(Unit) else Bad(One(CouldNotFindProofThatThePlayersFinished))
  }

  def formPlayers(players: List[PlayerId]) = {
    val durationSeconds = duelAccumulation.earliest.remaining.seconds
    for {
      playerId@PlayerId(name, ip) <- players
      hisStats = duelAccumulation.forPlayer(playerId)
      simplePlayerStatistics = SimplePlayerStatistics(
        name, ip, accuracy = hisStats.latest.accuracy.accuracy,
        frags = hisStats.latest.frags.frags,
        weapon = hisStats.view.groupBy(_.weapon).toList.sortBy(_._2.size).head._1.weapon,
        fragLog = {
          val first = hisStats.view.groupBy(stat => Math.ceil((durationSeconds - stat.remaining.seconds) / 60.0)).mapValues(_.minBy(_.remaining.seconds)).toList.sortBy(_._1)
          first.map(eh => eh._1.toInt -> eh._2.frags.frags).filterNot(_._1 == 0)
        }
      )
    } yield name -> simplePlayerStatistics
  }

  def allPlayersStillHere = {
    val bv = duelAccumulation.playerIds.forall(playerId =>
      duelAccumulation.view.exists(logItem =>
        Math.abs(logItem.remaining.seconds - timeRemaining.seconds) <= 5 &&
          logItem.playerId == playerId))
    if (bv) Good(Unit) else Bad(One(PlayersDisappeared))
  }

  def liveDuel: LiveDuel Or Every[DuelParseError] = {
    for {
      twoPlayers <- twoPlayersOr
      startedSeconds <- startedSecondsOr
      bothPlayersStarted <- allPlayersStartedOr(startedSeconds)
      _ <- allPlayersStillHere
      durationMinutes = saidDurationMinutes
      isADuelGame <- if (Set("ffa", "instagib", "efficiency") contains gameHeader.mode) Good(true) else Bad(One(FoundModeRejecting(gameHeader.mode)))
      players = formPlayers(twoPlayers)
    } yield LiveDuel(
      simpleId = s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
      duration = durationMinutes,
      serverDescription = CleanupDescription(gameHeader.startMessage.description),
      playedAt = duelAccumulation.view.map(_.remaining.seconds).map(t => (t / 60) + 1).distinct.sorted.toList,
      startTimeText = gameHeader.startTimeText,
      startTime = gameHeader.startTime,
      map = gameHeader.map,
      mode = gameHeader.mode,
      server = gameHeader.server,
      players = players.toMap, winner = None, metaId = None,
      secondsRemaining = timeRemaining.seconds
    )
  }

  def saidDurationMinutes = {
    val durationSeconds = duelAccumulation.earliest.remaining.seconds
    Math.ceil(durationSeconds / 60.0).toInt
  }

  def calculatedDurationMinutesOr = {
    val durationSeconds = duelAccumulation.earliest.remaining.seconds
    val foundDurationSeconds = durationSeconds - duelAccumulation.latest.remaining.seconds
    val foundDurationMinutes = Math.ceil(foundDurationSeconds / 60.0).toInt
    if (foundDurationMinutes >= 8) Good(foundDurationMinutes) else Bad(One(Expected8MinutesToDuel(foundDurationMinutes, foundDurationSeconds)))
  }

  def completeDuel(nextMessage: Option[ConvertedServerInfoReply]): SimpleCompletedDuel Or Every[DuelParseError] = {
    for {
      twoPlayers <- twoPlayersOr
      startedSeconds <- startedSecondsOr
      bothPlayersStarted <- allPlayersStartedOr(startedSeconds)
      durationMinutes = saidDurationMinutes
      foundDurationMinutes <- calculatedDurationMinutesOr
      bothPlayersFinished <- allPlayersFinishedOr(nextMessage)
      players = formPlayers(twoPlayers)
      isValidEfficGame <- {
        if (gameHeader.mode == "efficiency" && players.forall(_._2.frags >= 10))
          Good(Unit)
        else if (gameHeader.mode == "efficiency")
          Bad(One(EfficiencyExpectFragsOver10(players.map(_._2.frags))))
        else Good(Unit)
      }
      isValidInstagibGame <- {
        if (gameHeader.mode == "instagib" && players.forall(_._2.frags >= 20))
          Good(Unit)
        else if (gameHeader.mode == "instagib")
          Bad(One(InstaExpectFragsOver20(players.map(_._2.frags))))
        else Good(Unit)
      }
      isValidFFAGame <- {
        val sum = players.map(_._2.frags).sum
        if (gameHeader.mode == "ffa" && sum >= 15)
          Good(Unit)
        else if (gameHeader.mode == "ffa")
          Bad(One(FFAExpectSum15(sum)))
        else Good(Unit)
      }
      winner = if (players.map(_._2.frags).toSet.size == 1) None
      else {
        Option(players.maxBy(_._2.frags)._1)
      }
    } yield
      SimpleCompletedDuel(
        simpleId = s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
        duration = durationMinutes,
        playedAt = duelAccumulation.view.map(_.remaining.seconds).map(t => (t / 60) + 1).distinct.sorted.toList,
        serverDescription = CleanupDescription(gameHeader.startMessage.description),
        startTimeText = gameHeader.startTimeText,
        startTime = gameHeader.startTime,
        map = gameHeader.map,
        mode = gameHeader.mode,
        server = gameHeader.server,
        players = players.toMap, winner = winner, metaId = None
      )

  }
}

