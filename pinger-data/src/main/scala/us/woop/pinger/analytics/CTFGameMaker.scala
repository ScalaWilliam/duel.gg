package us.woop.pinger.analytics

import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.scalactic._
import org.scalactic.Accumulation._
import us.woop.pinger.analytics.better.BetterDuelMaker
import us.woop.pinger.analytics.worse.DuelMaker.GameHeader
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.{TeamScores, PlayerExtInfo, ParsedMessage}
import us.woop.pinger.data.{ModesList, Server}

object CTFGameMaker {

  case class PlayerId(name: String, ip: String, team: String)

  case class SimpleTeamStatistics(name: String, flags: Int, players: Map[PlayerId, Weapon])

  case class Team(name: String)

  case class SimpleCTF(duration: Int, playedAt: List[Int], teams: Map[Team, SimpleTeamStatistics])

  case class SimplePlayer(name: String, ip: String, weapon: String)

  case class SimpleTeamScore(name: String, flags: Int, flagLog: List[(Int, Int)], players: List[SimplePlayer])

  case class SimpleCompletedCTF
  (simpleId: String, teamsize:Int, duration: Int, playedAt: List[Int],
   startTimeText: String, startTime: Long, map: String, mode: String,
   server: String, teams: Map[String, SimpleTeamScore],
   winner: Option[String], metaId: Option[String]) {
    def toXml =
      <completed-ctf
        team-size={teamsize.toString}
        simple-id={simpleId}
        duration={duration.toString}
        start-time-raw={startTime.toString}
        start-time={startTimeText}
        map={map} mode={mode}
        server={server} winner={winner.orNull}
    meta-id={metaId.orNull}>
    {
    for { (team, scores) <- teams }
    yield <team name={team} flags={scores.flags.toString}>
    <flag-log>{for { (time, flags) <- scores.flagLog } yield <flags at={time.toString}>{flags}</flags>}</flag-log>
      {
        for { player <- scores.players} yield
          <player name={player.name} ip={player.ip} weapon={player.weapon}/>
      }
    </team>
    }
    </completed-ctf>
  }
  object SimpleCompletedCTF {
    def test = {
      val t =System.currentTimeMillis
      SimpleCompletedCTF(
        teamsize=2,
        simpleId = "yay",
        duration = 5,
        playedAt = List(1, 2, 3, 4, 5),
        startTimeText = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(t),
        startTime = t,
        map = "reissen",
        mode = "efficiency ctf",
        server = "localhost:123",
        winner = Option("evil"),
        teams = Map(
          "evil" -> SimpleTeamScore(name = "evil", 5, flagLog = List(1 -> 1, 2 -> 2, 3 -> 4, 4 -> 4, 5 -> 5), players = List(SimplePlayer(name = "Drakas", ip = "123", weapon = "rifle"))),
          "good" -> SimpleTeamScore(name = "good", 2, flagLog = List(1 -> 1, 2 -> 2, 3 -> 2, 4 -> 2, 5 -> 2), players = List(SimplePlayer(name = "Art", ip = "123", weapon = "rifle")))
        ),
        metaId = None
      )
    }
  }

  case class SecondsRemaining(seconds: Int)

  case class Flags(flags: Int)

  case class Weapon(weapon: String)

  case class Accuracy(accuracy: Int)

  case class TeamId(name: String)

  case class TeamLogItem(teamId: TeamId, remaining: SecondsRemaining, flags: Flags)

  case class PlayerLogItem(playerId: PlayerId, remaining: SecondsRemaining, weapon: Weapon)

  val ctfModeNames = Set("ctf", "insta ctf", "efficiency ctf")

  type StateTransition = PartialFunction[ParsedMessage, CtfState Or Every[ErrorMessage]]

  trait CtfState {
    def gameHeader: GameHeader

    def next: StateTransition
  }

  case class CtfAccumulation(teams: List[TeamLogItem], players: List[PlayerLogItem])

  object CTFGame {

    def beginCTFParsing(parsedMessage: ParsedMessage): CtfState Or Every[ErrorMessage] = {
      parsedMessage match {
        case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
          beginCtfCSIR(s, time, message)
        case other =>
          Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName} = ${other.message}"))
      }
    }

    def beginCtfCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): CtfState Or Every[ErrorMessage] = {

      // todo wut, no mastermode? wtf!

      val clients =
        if (message.clients >= 4) Good(message.clients)
        else Bad(One(s"Expected 4 or more clients, got ${message.clients}"))

      val ctfModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
        case Some(modeName) if ctfModeNames contains modeName => Good(modeName)
        case other => Bad(One(s"Mode $other (${message.gamemode}) not a ctf mode, expected one of $ctfModeNames"))
      }

      val hasEnoughTime =
        if (message.remain > 540) Good(true)
        else Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)"))

      withGood(clients, ctfModeName, hasEnoughTime) { (_, modeName, _) =>
        val gameHeader = GameHeader(startTime, message, s"${server.ip.ip}:${server.port}", modeName, message.mapname)
        val ctfAccumulation = CtfAccumulation(List.empty, List.empty)
        TransitionalCtf(
          gameHeader = gameHeader,
          ctfAccumulation = ctfAccumulation,
          isRunning = !message.gamepaused,
          timeRemaining = message.remain
        )
      }
    }
  }

  case class CtfFound(gameHeader: GameHeader, completedCtf: SimpleCompletedCTF) extends CtfState {
    lazy val next: StateTransition = throw new NotImplementedError("Should not get here...")
  }

  case class TransitionalCtf(gameHeader: GameHeader, isRunning: Boolean, timeRemaining: Int, ctfAccumulation: CtfAccumulation) extends CtfState {
    override def next: StateTransition = {
      case ParsedMessage(_, time, TeamScores(_, _, _, scores)) if isRunning =>
        val addition = scores.map { score =>
          TeamLogItem(TeamId(score.name), remaining = SecondsRemaining(timeRemaining), flags = Flags(score.score))
        }
        Good(this.copy(ctfAccumulation = ctfAccumulation.copy(teams = ctfAccumulation.teams ++ addition)))
      case ParsedMessage(_, time, info: PlayerExtInfo) if info.state <= 3 && isRunning =>
        val playerId = PlayerId(info.name, info.ip, info.team)
        val playerWeapon = Weapon(ModesList.guns.getOrElse(info.gun, "unknown"))
        val logItem = PlayerLogItem(playerId, SecondsRemaining(timeRemaining), weapon = playerWeapon)
        val newAccumulation = ctfAccumulation.copy(players = ctfAccumulation.players :+ logItem)
        Good(this.copy(ctfAccumulation = newAccumulation))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) if info.remain == 0 =>
        Good(copy(isRunning = true, timeRemaining = 0))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) if BetterDuelMaker.isSwitch(gameHeader.startMessage, info) =>
        if (ctfAccumulation.players.isEmpty) Bad(One("Player log empty"))
        else if (ctfAccumulation.teams.isEmpty) Bad(One("Team log empty"))
        else completeCtf(this)(Option(info)).map(CtfFound(gameHeader, _))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) =>
        Good(copy(isRunning = !info.gamepaused, timeRemaining = info.remain))
      case other => Good(this)
    }
  }

  def completeCtf(transitionalCtf: TransitionalCtf)(nextMessage: Option[ConvertedServerInfoReply]): SimpleCompletedCTF Or Every[ErrorMessage] = {
    /**
     * Start with an even number of players of at least six.
     * Expect at least 1 player from each team to stay to the very end.
     */
    import transitionalCtf.gameHeader
    for {
      startedAppropriateNumberOfPlayers <- {
        val maxRemaining = transitionalCtf.ctfAccumulation.players.map(_.remaining.seconds).max
        val activePlayersAtStart = transitionalCtf.ctfAccumulation.players.filter(_.remaining.seconds == maxRemaining)
        val activeTeamsAtStart = activePlayersAtStart.groupBy(_.playerId.team)
        val teamPlayerCounts = activeTeamsAtStart.map(_._2.size).toSet
        if (activeTeamsAtStart.size != 2) {
          Bad(One(s"Expected two teams at start, found ${activeTeamsAtStart.size}"))
        } else if (teamPlayerCounts.size != 1) {
          Bad(One(s"Expected two teams to start with same number of players, found ${teamPlayerCounts.size}"))
        } else if (teamPlayerCounts.head < 2) {
          Bad(One(s"1v1 CTF not supported, found ${teamPlayerCounts.head} players"))
        } else {
          Good(activePlayersAtStart.size)
        }
      }
      teams = transitionalCtf.ctfAccumulation.teams.map(_.teamId).toSet
      maxRemainingPlayers = transitionalCtf.ctfAccumulation.players.map(_.remaining.seconds).max
      maxRemainingTeams = transitionalCtf.ctfAccumulation.teams.map(_.remaining.seconds).max
      bothTeamsStarted = teams.forall(team =>
        transitionalCtf.ctfAccumulation.teams.exists { logItem =>
          logItem.teamId == team && logItem.remaining.seconds == maxRemainingTeams
        })
      lastTime = transitionalCtf.ctfAccumulation.teams.map(_.remaining.seconds).min
      _ <- if (lastTime <= 4) Good(Unit) else Bad(One(s"Last remaining time found was $lastTime, expected below 4."))
      bothTeamsFinished = teams.forall(team =>
        transitionalCtf.ctfAccumulation.teams.exists { logItem =>
          logItem.remaining.seconds == lastTime && logItem.teamId == team
        }
      )
      atMostTwoPlayersLeftAtTheEnd <- {
        val playersRemaining = transitionalCtf.ctfAccumulation.players.count{ logItem =>
          logItem.remaining.seconds == maxRemainingPlayers
        }
        if ( playersRemaining > startedAppropriateNumberOfPlayers ) {
          Bad(One(s"Game ended with more players ($playersRemaining) than started ($startedAppropriateNumberOfPlayers)"))
        } else if ( startedAppropriateNumberOfPlayers - playersRemaining > 2 ) {
          Bad(One(s"Started with $startedAppropriateNumberOfPlayers, ended up with $playersRemaining, too many people left at the end."))
        } else {
          Good(Unit)
        }
      }
      _ <- if (bothTeamsStarted) Good(Unit) else Bad(One("Could not find a team log item to say that both teams started the game"))
      _ <- if (bothTeamsFinished) Good(Unit) else Bad(One(s"Could not find a team log item to stay that both teams finished the game ($nextMessage)"))
    playedAt <- {
      val plays = transitionalCtf.ctfAccumulation.teams.map(_.remaining.seconds).map(t => (t / 60) + 1).toSet.toList.sorted
      if ( plays.size < 8 ) Bad(One(s"Game active at $plays, expected more")) else Good(plays)
    }
      durationSeconds = maxRemainingTeams
      durationMinutes = Math.ceil(durationSeconds / 60.0).toInt
      teamResults = {
        for {
          team <- teams
          teamStats = transitionalCtf.ctfAccumulation.teams.filter(_.teamId.name == team.name)
          flagLog = {
            val first = teamStats.groupBy(stat => Math.ceil((durationSeconds - stat.remaining.seconds) / 60.0)).mapValues(_.minBy(_.remaining.seconds)).toList.sortBy(_._1)
            first.map(eh => eh._1.toInt -> eh._2.flags.flags).filterNot(_._1 == 0)
          }
          TeamLogItem(_, _, flags) <- transitionalCtf.ctfAccumulation.teams.find(logItem => logItem.remaining.seconds == lastTime && logItem.teamId == team)
          teamPlayers = for {
            (player, playerLogs) <- transitionalCtf.ctfAccumulation.players.filter(_.playerId.team == team.name).groupBy(_.playerId)
            (favouriteWeapon, _) = playerLogs.groupBy(_.weapon).toList.sortBy(_._2.size).reverse.head
          } yield SimplePlayer(player.name, player.ip, favouriteWeapon.weapon)
          teamScore = SimpleTeamScore(name = team.name, flags = flags.flags, flagLog = flagLog, players = teamPlayers.toList)
        } yield team.name -> teamScore
      }
      winner = if (teamResults.map(_._2.flags).toSet.size == 1) None
      else {
        Option(teamResults.maxBy(_._2.flags)._1)
      }
    } yield SimpleCompletedCTF(
    teamsize = startedAppropriateNumberOfPlayers/2,
      simpleId = s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
      duration = durationMinutes,
      playedAt = playedAt,
      startTimeText = gameHeader.startTimeText,
      startTime = gameHeader.startTime,
      map = gameHeader.map,
      mode = gameHeader.mode,
      server = gameHeader.server,
      teams = teamResults.toMap, winner = winner, metaId = None)
  }
}