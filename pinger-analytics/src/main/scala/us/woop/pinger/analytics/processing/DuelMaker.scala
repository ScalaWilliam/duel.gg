package us.woop.pinger.analytics.processing

import org.joda.time.format.ISODateTimeFormat
import org.scalactic.Accumulation._
import org.scalactic._
import us.woop.pinger.analytics.data.ModesList
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.{ParsedMessage, PlayerExtInfo}
import us.woop.pinger.data.persistence.Format.Server

import scala.collection.immutable.{SortedMap, SortedSet}

object DuelMaker {

 case class PlayerLog(fragsLog: SortedMap[Long, Int], weaponsLog: SortedMap[Long, Int])
  
  case class PlayerStatistics(frags: Int, fragsLog: SortedMap[Int, Int], weapon: Int)
  
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
                            playedAt: Set[Int], duration: Int) extends DuelState {
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
          gameHeader = GameHeader(startTime, message, s"${server.ip}:${server.port}", modeName, message.mapname),
          playerLogs = Map.empty,
          playing = !message.gamepaused,
          playingAt = SortedSet.empty
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
        val player = playerLogs.getOrElse(playerId, PlayerLog(SortedMap.empty, SortedMap.empty))
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
          weapon = player.weaponsLog.groupBy(_._2).mapValues(_.size).maxBy(_._2)._1
        )
        secondlyPlayerStatistics.copy(
          fragsLog = SortedMap(secondlyPlayerStatistics.fragsLog.groupBy(_._1 / 60000).mapValues(_.maxBy(_._1)._2).toVector :_*)
          .map{case(t, v) => (t + 1) -> v}.filterKeys(_ <= 10)
        )
      })

      // per-minute activity of games
      val gameActive = playingAt.map(_ - gameHeader.startTime).map(_.toInt).groupBy(_ / 60000).mapValues(_.max / 60000).values.map(_+1).filter(_<=10).toSet

      val activeForOver8Minutes =
        if (gameActive.size >= 8) Good(gameActive)
        else Bad(One(s"Game was active ${gameActive.size} minutes, expected at least 8 minutes"))

      val totalNumberOfPlayers =
        if (playerStatistics.size == 2) Good(playerStatistics)
        else Bad(One(s"Game had != 2 players, found $playerStatistics"))

      withGood(activeForOver8Minutes, totalNumberOfPlayers) {
        (gameActivity, playerStats) =>
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

}
