package pinger

import us.woop.pinger.data.actor.PingPongProcessor.Server
import us.woop.pinger.Collector.GameData
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.ParsedPongs.TypedMessages.{ParsedTypedMessage, ParsedTypedMessageConversion}
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.{ParsedTypedMessageConvertedTeamScore, ParsedTypedMessageConvertedServerInfoReply, ParsedTypedMessagePlayerExtInfo}

object ClanmatchMaker {



    case class Clanmatch(timestamp: String, server: Server, map: String, mode: String, winner: Option[String], gameDuration: Int, teams: Map[String, Team], playing: Boolean, activeAt: List[Int])
    case class Team(name: String, scoresLog: Map[Int, Int], score: Int, players: Map[String, Player])
    case class Player(name: String, ip: String, weaponsLog: Map[Int, Int], mainWeapon: Int)
    def makeDuel(gameData: GameData) = {

      val initialCw = {
        import gameData.firstTime._
        import message._
        Clanmatch(timestamp = ISODateTimeFormat.dateTimeNoMillis().print(DateTime.now), server = server, map = mapname, mode = gamemode.map {
          _.toString
        }.getOrElse(""), winner = None, gameDuration = 0, teams = Map.empty, playing = true, activeAt = List.empty)
      }

      object PlayerInfoUpdate {
        def unapply(msg: ParsedMessage) = for {
          ParsedTypedMessagePlayerExtInfo(ParsedTypedMessage(_, _, playerExtInfo)) <- Option(msg)
        } yield playerExtInfo
      }
      object ServerInfoUpdate {
        def unapply(msg: ParsedMessage) = for {
          ParsedTypedMessageConvertedServerInfoReply(ParsedTypedMessage(_, _, serverInfo)) <- Option(msg)
        } yield serverInfo
      }
      object TeamScoreUpdate {
        def unapply(msg: ParsedMessage) = for {
          ParsedTypedMessageConvertedTeamScore(ParsedTypedMessage(_, _, teamScore)) <- Option(msg)
        } yield teamScore
      }

      val clanwarCalculation = gameData.data.foldLeft[Either[String, Clanmatch]](Right(initialCw)){

        case (fault @ Left(_), _) => fault

        case (Right(cw), ServerInfoUpdate(serverUpdate)) if cw.playing && serverUpdate.gamepaused =>
          Right(cw.copy(playing = !cw.playing, gameDuration = cw.gameDuration + 3))

        case (Right(cw), ServerInfoUpdate(serverUpdate)) if !cw.playing && !serverUpdate.gamepaused =>
          Right(cw.copy(playing = !cw.playing, gameDuration = cw.gameDuration + 3, activeAt = cw.activeAt :+ cw.gameDuration))

        case (Right(cw), ServerInfoUpdate(serverUpdate)) if cw.playing =>
          Right(cw.copy(activeAt = cw.activeAt :+ cw.gameDuration, gameDuration = cw.gameDuration + 3))

        case (Right(cw), PlayerInfoUpdate(info)) if info.state < 5 =>
          val team = cw.teams.getOrElse(info.team, Team(name = info.team, scoresLog = Map.empty, score = 0, players = Map.empty))
          val player = team.players.getOrElse(info.name, Player(name = info.name, ip = info.ip, weaponsLog = Map.empty, mainWeapon = 0))
          val updatedTeam = team.copy(players = team.players.updated(player.name, player))
          val updatedTeams = cw.teams.updated(updatedTeam.name, updatedTeam)
          val updatedCw = cw.copy(teams = updatedTeams)
          if ( updatedCw.teams.size > 2 ) {
            Left(s"There are more than two teams (cause: ${info.name} on ${info.team}")
          } else {
            Right(updatedCw)
          }
        case (Right(cw), TeamScoreUpdate(teamScore)) =>
          if ( cw.teams contains teamScore.name ) {
            val updatedTeam = cw.teams(teamScore.name).copy(score = teamScore.scoreNum)
            Right(cw.copy(teams = cw.teams.updated(teamScore.name, updatedTeam)))
          } else Right(cw)

        case (x, _) => x
      }

      for {
        cw <- clanwarCalculation.right
        matching <- if (cw.gameDuration > 280) Right(cw) else Left(s"Game only lasted ${cw.gameDuration}")
      } yield
        <duel>
          <server>{cw.server}</server>
          <map>{cw.map}</map>
          <timestamp>{cw.timestamp}</timestamp>
          <duration>{cw.gameDuration}</duration>
          <mode>{cw.mode}</mode>{
          val sortedByScore = cw.teams.mapValues{_.score}.toList.sortBy{_._2}
          if ( sortedByScore.map{_._2}.toSet.size > 1 ) {
            val (name, winner) = sortedByScore.last
            <winner>
              {name}
            </winner>
          }
          }
          {
          for {
            (name, team) <- cw.teams
          } yield <team>
            <name>{name}</name>
            <score>{team.score}</score>
            {for {
              (_, player) <- team.players
            } yield <player>
                <name>{player.name}</name>
                <ip>{player.ip}</ip>
                <weapon>{player.mainWeapon}</weapon>
              </player>}
          </team>
          }
        </duel>
    }
}
