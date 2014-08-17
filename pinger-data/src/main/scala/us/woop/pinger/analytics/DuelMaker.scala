package us.woop.pinger.analytics

import org.joda.time.format.ISODateTimeFormat
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.{ParsedMessage, PlayerExtInfo}
import us.woop.pinger.data.{ModesList, Server}

import scala.collection.immutable.SortedMap
import org.scalactic._
import org.scalactic.Accumulation._
object DuelMaker {

  case class PlayerLog(fragsLog: Map[Long, Int], weaponsLog: Map[Long, Int])
  
  case class PlayerStatistics(frags: Int, fragsLog: Map[Int, Int], weapon: String)
  
  val duelModeNames = Set("ffa", "instagib", "efficiency")

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  type StateTransition = PartialFunction[ParsedMessage, DuelState Or Every[ErrorMessage]]

  trait DuelState {
    def gameHeader: GameHeader
    def next: StateTransition
  }

  case class CompletedDuel(gameHeader: GameHeader, nextMessage: Option[ConvertedServerInfoReply],
                           winner: Option[(PlayerId, PlayerStatistics)],
                            playerStatistics: Map[PlayerId, PlayerStatistics],
                            playedAt: Set[Int], duration: Int, metaId: Option[String] = None) extends DuelState {
    lazy val next: StateTransition = throw new NotImplementedError("Should not get here...")
    override def toString =
      s"""|Header: $gameHeader
         |Winner: $winner,
         |Player statistics:
         |${playerStatistics mkString "\n"}
         |Duration: $duration
         |Play activity: $playedAt
         |Next message: $nextMessage
       """.stripMargin


    def toSimpleCompletedDuel = {
      SimpleCompletedDuel(
        simpleId= s"${gameHeader.startTimeText}::${gameHeader.server}".replaceAll("[^a-zA-Z0-9\\.:-]", ""),
        duration = duration,
        playedAt = playedAt,
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
            frags = playerStats.frags,
            weapon = playerStats.weapon,
            fragLog = playerStats.fragsLog.map { case (x, y) => s"$x" -> y}
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
      winner = Option(PlayerId("Test", "a.b.c.x") -> PlayerStatistics(frags = 5, fragsLog = Map(1 -> 2), weapon = "test")),
      playerStatistics = Map(
        PlayerId("Test", "a.b.c.x") -> PlayerStatistics(frags = 5, fragsLog = Map(1 -> 2), weapon = "test"),
          PlayerId("Best", "a.b.c.x") -> PlayerStatistics(frags = 20, fragsLog = Map(1 -> 2, 3-> 4), weapon = "rocket launcher")
      ),
      playedAt = Set(1,2,5),
      duration = 5
    )

  }

  case class GameHeader(startTime: Long, startMessage: ConvertedServerInfoReply, server: String, mode: String, map: String) {
    def startTimeText: String = ISODateTimeFormat.dateTimeNoMillis().print(startTime)
    override def toString =
      s"""|Server: $server
         |Mode: $mode
         |Map: $map
         |Start time: $startTimeText
         |Start message: $startMessage
       """.stripMargin
  }
  object GameHeader {
    val testServer = "test:1234"
    def test = GameHeader(
      startTime = System.currentTimeMillis,
      startMessage = ConvertedServerInfoReply(0,0,0,0,0,true,0,"test","test"),
      server = testServer, mode = "test", map = "test"
    )
  }

  case class PlayerId(name: String, ip: String)

  object Duel {
    
    def beginDuelParsedMessage(parsedMessage: ParsedMessage): DuelState Or Every[ErrorMessage] = {
      parsedMessage match {
        case ParsedMessage(s, time, message: ConvertedServerInfoReply) =>
          beginDuelCSIR(s, time, message)
        case other =>
          Bad(One(s"Input not a ConvertedServerInfoReply, found ${other.message.getClass.getName}"))
      }
    }

    def beginDuelCSIR(server: Server, startTime: Long, message: ConvertedServerInfoReply): DuelState Or Every[ErrorMessage] = {

      val clients =
        if (message.clients >= 2) Good(message.clients)
        else Bad(One(s"Expected 2 or more clients, got ${message.clients}"))

      val duelModeName = ModesList.modes.get(message.gamemode).map(_.name) match {
        case Some(modeName) if duelModeNames contains modeName  => Good(modeName)
        case other => Bad(One(s"Mode $other (${message.gamemode})  not in $duelModeNames"))
      }

      val hasEnoughTime =
        if ( message.remain > 550 ) Good(true)
        else Bad(One(s"Time remaining not enough: ${message.remain} (expected 550+ seconds)"))

      withGood(clients, duelModeName, hasEnoughTime) { (_, modeName, _) =>
        DuelTransitive(
          gameHeader = GameHeader(startTime, message, s"${server.ip.ip}:${server.port}", modeName, message.mapname),
          playerLogs = Map.empty,
          playing = !message.gamepaused,
          playingAt = Set.empty
        )
      }
    }
  }


  case class DuelTransitive(gameHeader: GameHeader,
                            playerLogs: Map[PlayerId, PlayerLog],
                  playing: Boolean, playingAt: Set[Long]) extends DuelState {

    override val next: StateTransition = {
      case ParsedMessage(_, time, info: PlayerExtInfo) if info.state <= 3 =>
        val playerId = PlayerId(info.name, info.ip)
        val player = playerLogs.getOrElse(playerId, PlayerLog(Map.empty, Map.empty))
        Good(copy(
          playerLogs = playerLogs.updated(
            playerId,
            player.copy(
              fragsLog = player.fragsLog.updated(time, info.frags),
              weaponsLog = player.weaponsLog.updated(time, info.gun)
            )
          )
        ))
      case ParsedMessage(_, time, info: ConvertedServerInfoReply) if isSwitch(gameHeader.startMessage, info) =>
        completeDuel(Option(info))
      case ParsedMessage(_, time, update: ConvertedServerInfoReply) if !update.gamepaused && update.clients < 2 =>
        Bad(One(s"Game has finished unexpectedly as now ${update.clients} clients"))
      case ParsedMessage(_, time, update: ConvertedServerInfoReply) if !update.gamepaused =>
        Good(copy(
          playingAt = playingAt + time
        ))
      case other => Good(this)
    }

    def completeDuel(nextMessage: Option[ConvertedServerInfoReply]): CompletedDuel Or Every[ErrorMessage] = {

      // per-minute player statistics (for frags log)
      val playerStatistics = playerLogs.mapValues(player => {
        val secondlyPlayerStatistics = PlayerStatistics(
          frags = player.fragsLog.maxBy(_._1)._2,
          fragsLog = player.fragsLog.map{case (a, b) => (a - gameHeader.startTime).toInt-> b},
          weapon = {
            val weaponId = player.weaponsLog.groupBy(_._2).mapValues(_.size).maxBy(_._2)._1
            ModesList.guns.getOrElse(weaponId, "unknown")
          }
        )
        val thirdly = secondlyPlayerStatistics.copy(
          fragsLog = Map(SortedMap(secondlyPlayerStatistics.fragsLog.groupBy(_._1 / 60000).mapValues(_.maxBy(_._1)._2).toVector :_*).map{case(t, v) => (t + 1) -> v}.filterKeys(_ <= 10).toVector :_*)
        )
        thirdly.copy(
          frags = thirdly.fragsLog.maxBy(_._1)._2
        )
      }).map(identity)

      val haveStatsForEndOfGame = {
        if ( playerStatistics.values.exists(_.fragsLog.isEmpty))
          Bad(One(s"Player had an empty frag log"))
        else {
          val latestPlayerStatistic = playerStatistics.values.map(_.fragsLog.keys.max)
          if (latestPlayerStatistic.toSet.size == 1) Good(latestPlayerStatistic)
          else Bad(One(s"Player stats were unavailable at the end of the game: have $latestPlayerStatistic"))
        }
      }

      // per-minute activity of games
      val gameActive = playingAt.map(_ - gameHeader.startTime).map(_.toInt).groupBy(_ / 60000).mapValues(_.max / 60000).values.map(_+1).filter(_<=10).toSet

      val activeForOver8Minutes =
        if (gameActive.size >= 8) Good(gameActive)
        else Bad(One(s"Game was active ${gameActive.size} minutes, expected at least 8 minutes"))

      val totalNumberOfPlayers =
        if (playerStatistics.size == 2) Good(playerStatistics)
        else Bad(One(s"Game had != 2 players, found $playerStatistics"))

      val uniquePlayerNames = {
        val uniqueNames = playerStatistics.keySet.map(_.name)
        if (uniqueNames.size == 2) Good(true)
        else Bad(One(s"Game does not have unique player names. Found: $uniqueNames"))
      }

      withGood(haveStatsForEndOfGame, activeForOver8Minutes, totalNumberOfPlayers, uniquePlayerNames) {
        (_, gameActivity, playerStats, _) =>
          CompletedDuel(
            winner = Option {
              playerStats.groupBy(_._2.frags).filter(_._2.size == 1)
            }.filter(_.nonEmpty).flatMap(_.maxBy(_._2.size)._2.headOption),
            gameHeader = gameHeader,
            nextMessage = nextMessage,
            playerStatistics = playerStats,
            playedAt = gameActivity,
            duration = gameActivity.max
          )
      }
    }
  }


  case class SimplePlayerStatistics
  (
    name: String,
    ip: String,
    frags: Int,
    weapon: String,
    fragLog: Map[String, Int])

  case class SimpleCompletedDuel
  (
  simpleId: String,
    duration: Int,
    playedAt: Set[Int],
    startTimeText: String,
    startTime: Long,
    map: String,
    mode: String,
    server: String,
    players: Map[String, SimplePlayerStatistics],
    winner: Option[String], metaId: Option[String]) {
    def toJson = {
      import org.json4s._
      import org.json4s.native.Serialization
      import org.json4s.native.Serialization.write
      implicit val formats = Serialization.formats(NoTypeHints)
      write(this)
    }
    def toPrettyJson = {
      import org.json4s._
      import org.json4s.native.Serialization
      import org.json4s.native.Serialization.writePretty
      implicit val formats = Serialization.formats(NoTypeHints)
      writePretty(this)
    }
    def toXml = <completed-duel
    simple-id={simpleId}
    meta-id={metaId.orNull}
    duration={s"$duration"}
    start-time={startTimeText}
    map={map}
    mode={mode}
    server={server}
    winner={winner.orNull}>
      <played-at>{playedAt.mkString(" ")}</played-at>
      <players>
        {for {(name, stats) <- players} yield
        <player name={name} ip={stats.name} frags={s"${stats.frags}"}
                weapon={stats.weapon}>
          {for {(at, frags) <- stats.fragLog}
        yield
          <frags at={at}>{frags}</frags>}
        </player>
        }
      </players>
    </completed-duel>


  }
  object Yay {

  }

}
