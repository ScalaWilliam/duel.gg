package us.woop.pinger.analytics.better

import org.scalactic.Accumulation._
import org.scalactic._
import us.woop.pinger.analytics.worse.DuelMaker
import us.woop.pinger.analytics.worse.DuelMaker.{SimplePlayerStatistics, GameHeader, SimpleCompletedDuel}
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.{PartialPlayerExtInfo, ParsedMessage, PlayerExtInfo}
import us.woop.pinger.data.{ModesList, Server}

object BetterDuelMaker {

  case class SecondsRemaining(seconds: Int)
  case class Frags(frags: Int)
  case class Weapon(weapon: String)
  case class Accuracy(accuracy: Int)
  case class LogItem(playerId: PlayerId, remaining: SecondsRemaining, frags: Frags, weapon: Weapon, accuracy: Accuracy)

  val duelModeNames = Set("ffa", "instagib", "efficiency")
  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  case class PlayerId(name: String, ip: String)
  type StateTransition = PartialFunction[ParsedMessage, BetterDuelState Or Every[ErrorMessage]]

  trait BetterDuelState {
    def gameHeader: GameHeader
    def next: StateTransition
  }

  case class DuelAccumulation(playerStatistics: List[LogItem])

  object Duel {

    def beginDuelParsedMessage(parsedMessage: ParsedMessage): BetterDuelState Or Every[ErrorMessage] = {
      parsedMessage match {
        case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
          beginDuelCSIR(s, time, message)
        case other =>
          Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName} = ${other.message}"))
      }
    }

    def beginDuelCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): BetterDuelState Or Every[ErrorMessage] = {

      val clients =
        if (message.clients >= 2) Good(message.clients)
        else Bad(One(s"Expected 2 or more clients, got ${message.clients}"))

      val duelModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
        case Some(modeName) if duelModeNames contains modeName  => Good(modeName)
        case other => Bad(One(s"Mode $other (${message.gamemode}) not a duel mode, expected one of $duelModeNames"))
      }

      val hasEnoughTime =
        if ( message.remain > 540 ) Good(true)
        else Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)"))

      withGood(clients, duelModeName, hasEnoughTime) { (_, modeName, _) =>
        val gameHeader = GameHeader(startTime, message, s"${server.ip.ip}:${server.port}", modeName, message.mapname)
        val duelAccumulation = DuelAccumulation(List.empty)
        TransitionalBetterDuel(
          gameHeader = gameHeader,
          duelAccumulation = duelAccumulation,
          isRunning = !message.gamepaused,
          timeRemaining = message.remain
        )
      }
    }
  }

  case class BetterDuelFound(gameHeader: GameHeader, simpleCompletedDuel: SimpleCompletedDuel) extends BetterDuelState {
    lazy val next: StateTransition = throw new NotImplementedError("Should not get here...")
  }

  // when we get to 0, we wish to wait for the next bunch of extinfos first.
//  case class NearlyFinished(gameHeader: GameHeader, duelAccumulation: DuelAccumulation) extends BetterDuelState {
//    override def next: StateTransition = {
//      case ParsedMessage(_, time, info: PlayerExtInfo)
//    }
//  }

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
        completeDuel(this)(Option(info)).map(BetterDuelFound(gameHeader, _))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 =>
        Good(copy(isRunning = true, timeRemaining = 0))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) if isSwitch(gameHeader.startMessage, info) =>
        completeDuel(this)(Option(info)).map(BetterDuelFound(gameHeader, _))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) =>
        Good(copy(isRunning = !info.gamepaused, timeRemaining = info.remain))
      case other => Good(this)
    }

  }


  def completeDuel(transitionalDuel:TransitionalBetterDuel)(nextMessage: Option[ConvertedServerInfoReply]): SimpleCompletedDuel Or Every[ErrorMessage] = {
    import transitionalDuel.duelAccumulation.playerStatistics
    import transitionalDuel.gameHeader
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
      _ <- if ( bothPlayersStarted ) Good(Unit) else Bad(One("Could not find a log item to say that both players started the game"))
      _ <- if ( bothPlayersFinished ) Good(Unit) else Bad(One(s"Could not find a log item to say that both players finished the game ($nextMessage)"))
      durationSeconds = playerStatistics.head.remaining.seconds
      durationMinutes = Math.ceil(durationSeconds / 60.0).toInt
      players = {
        for {
          playerId @ PlayerId(name, ip) <- twoPlayers
          hisStats = playerStatistics.filter(_.playerId == playerId)
          simplePlayerStatistics = SimplePlayerStatistics(
            name, ip, accuracy = hisStats.last.accuracy.accuracy,
            frags = hisStats.last.frags.frags,
            weapon = hisStats.groupBy(_.weapon).toList.sortBy(_._2.size).head._1.weapon,
            fragLog = {
              val first = hisStats.groupBy(stat => Math.ceil((durationSeconds - stat.remaining.seconds)/60.0)).mapValues(_.minBy(_.remaining.seconds)).toList.sortBy(_._1)
              first.map(eh=> eh._1.toInt -> eh._2.frags.frags).filterNot(_._1 == 0)
            }
          )
        } yield name -> simplePlayerStatistics
      }
      winner = if ( players.map(_._2.frags).toSet.size == 1 ) None else {
        Option(players.maxBy(_._2.frags)._1)
      }
    } yield
      SimpleCompletedDuel(
        simpleId= s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
        duration = durationMinutes,
        playedAt = playerStatistics.map(_.remaining.seconds).map(t => (t / 60) + 1).toSet.toList.sorted,
        startTimeText = gameHeader.startTimeText,
        startTime = gameHeader.startTime,
        map = gameHeader.map,
        mode = gameHeader.mode,
        server = gameHeader.server,
        players = players.toMap, winner = winner, metaId = None
      )

  }

}

