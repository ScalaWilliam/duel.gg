package pinger

import us.woop.pinger.data.ParsedProcessor.ParsedTypedMessage
import us.woop.pinger.Collector.GameData
import us.woop.pinger.data.ParsedProcessor.ParsedTypedMessages.{ParsedTypedMessageConvertedTeamScore, ParsedTypedMessagePlayerExtInfo}
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo

object GameMaker {
  import concurrent.duration._
  def process(data: GameData) = {
    case class Player(name: String, ip: String)
    case class Team(name: String)
    case class TeamPlayer(player: Player, team: Team)
    case class ExtExt(message: ParsedTypedMessage[PlayerExtInfo], player: Player, teamPlayer: Option[TeamPlayer])
    for {
      game <- Option(data)
      mode <- game.firstTime.message.gamemode
      server = game.firstTime.server
      if (1 to 22) contains mode.id
      if mode.id != 2
      playerExtInfos = game.data.collect {
        case ParsedTypedMessagePlayerExtInfo(message @ ParsedTypedMessage(_, _, playerExtInfo)) if playerExtInfo.state != 999 =>
          val player = Player(playerExtInfo.name, playerExtInfo.ip)
          ExtExt(message, player, Option(playerExtInfo.team).filterNot(_=="").map{team=>TeamPlayer(player, Team(team))})
      }
      perPlayer = playerExtInfos.groupBy{_.player}
      playTimes = perPlayer.mapValues { items =>
        for { a :: b:: Nil <- items.sliding(2)
              timePlayed = (b.message.time - a.message.time).millis
              if timePlayed < 6.seconds } yield timePlayed }.mapValues{_.map{_.toSeconds}.sum.seconds}
      if playTimes.size > 1
      playerFrags = perPlayer.mapValues{_.map{_.message.message.frags}.max}.filter{_._2 > 0}
      playerDeaths = perPlayer.mapValues{_.map{_.message.message.deaths}.max}
      playerAccuracy = perPlayer.mapValues{l => l.map{_.message.message.accuracy}.sum / l.length}
      playerTeamkills = perPlayer.mapValues{l => l.map{_.message.message.teamkills}.max}.filter{_._2 > 0}
      playerGun = perPlayer.mapValues{_.map{_.message.message.gun}.groupBy{identity}.mapValues{_.size}.maxBy{_._2}._1}
      teamsPlayers = playerExtInfos.flatMap{_.teamPlayer}.groupBy{_.team}
      teamScores = game.data.collect {
        case ParsedTypedMessageConvertedTeamScore(ParsedTypedMessage(_, _, teamScore)) =>
          Team(teamScore.name) -> teamScore.score
      }.groupBy{_._1}.mapValues{_.maxBy{_._2}._2}
    }
    yield
      <game>
        <server>
          <host>{server.ip.ip}:{server.port}</host>
          <description>{data.firstTime.message.description}</description>
        </server>
      <mode>{mode.id}</mode>
      <map>{data.firstTime.message.mapname}</map>
        {
        for {
          (team, teamPlayers) <- teamsPlayers
          score <- teamScores get team
        }
          yield <team>
          <name>{team.name}</name>
          <score>{score}</score>
          {for {teamPlayer <- teamPlayers
          time <- playTimes get teamPlayer.player
            if time > 2.minutes
          } yield <player>
            <name>{teamPlayer.player.name}</name>
            <ip>{teamPlayer.player.ip}</ip>
          </player>}
        </team>
        }

        {
        for {
          (player, time) <- playTimes
          if time > 2.minutes
          frags <- playerFrags get player
          deaths <- playerDeaths get player
          accuracy <- playerAccuracy get player
          weapon <- playerGun get player
        } yield <player>
        <name>{player.name}</name>
        <ip>{player.ip}</ip>
          <frags>{frags}</frags>
          <deaths>{deaths}</deaths>
          <accuracy>{accuracy}</accuracy>
          <weapon>{weapon}</weapon>
          {for { tk <- (playerTeamkills get player).toSeq } yield <teamkills>{tk}</teamkills> }
        </player>
        }
    </game>
  }
}
