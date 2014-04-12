package pinger

import us.woop.pinger.Collector.GameData
import org.joda.time.format.ISODateTimeFormat
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessage
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.{ParsedTypedMessageConvertedServerInfoReply, ParsedTypedMessagePlayerExtInfo}
import us.woop.pinger.data.actor.PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import org.joda.time.DateTime

object DuelMaker {

  case class Duel(timestamp: String, server: Server, map: String, mode: String, winner: Option[String], gameDuration: Int, players: Map[String, Player], playing: Boolean, activeAt: List[Int])

  case class Player(name: String, ip: String, fragsLog: Map[Int, Int], weaponsLog: Map[Int, Int], frags: Int, mainWeapon: Int)

  def makeDuel(gameData: GameData) = {

    val initialDuel = {
      import gameData.firstTime._
      import message._
      Duel(timestamp = ISODateTimeFormat.dateTimeNoMillis().print(DateTime.now), server = server, map = mapname, mode = gamemode.map {
        _.toString
      }.getOrElse(""), winner = None, gameDuration = 0, players = Map.empty, playing = true, activeAt = List.empty)
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

    type PotentialGame = Either[String, Duel]
    val duelCalculation = gameData.data.foldLeft[PotentialGame](Right(initialDuel)){

      case (fault @ Left(_), _) => fault

      case (Right(duel), ServerInfoUpdate(serverUpdate)) if duel.playing && serverUpdate.gamepaused =>
        Right(duel.copy(playing = !duel.playing, gameDuration = duel.gameDuration + 3))

      case (Right(duel), ServerInfoUpdate(serverUpdate)) if !duel.playing && !serverUpdate.gamepaused =>
        Right(duel.copy(playing = !duel.playing, gameDuration = duel.gameDuration + 3, activeAt = duel.activeAt :+ duel.gameDuration))

      case (Right(duel), ServerInfoUpdate(serverUpdate)) if duel.playing =>
        Right(duel.copy(activeAt = duel.activeAt :+ duel.gameDuration, gameDuration = duel.gameDuration + 3))

      case (Right(duel), PlayerInfoUpdate(info)) if info.state < 5 =>
        val player = duel.players.getOrElse(info.name, Player(info.name, info.ip, Map.empty, Map.empty, frags = 0, mainWeapon = 0))
        if (player.ip != info.ip) {
          Left(s"IP for '${info.name}' has changed from '${player.ip}' to '${info.ip}. Discarding duel")
        } else {
          val newFragsLog = player.fragsLog.updated(duel.gameDuration, info.frags)
          val newWeaponsLog = player.weaponsLog.updated(duel.gameDuration, info.gun)
          val mainWeapon = newWeaponsLog.toList.map{_.swap}.groupBy{_._1}.mapValues{_.size}.toList.sorted.reverse.head._1
          val newPlayers =
            duel.players.updated(
              player.name,
              player.copy(
                fragsLog = newFragsLog,
                weaponsLog = newWeaponsLog,
                frags = info.frags,
                mainWeapon = mainWeapon
              )
            )
          if ( newPlayers.size > 2 ) {
            Left(s"More than 2 players in-game (${info.name} joined)")
          } else {
            Right(duel.copy(players = newPlayers))
          }
        }
    }

    for {
      duel <- duelCalculation.right
      matching <- if (duel.gameDuration > 280) Right(duel) else Left(s"Game only lasted ${duel.gameDuration}")
    } yield 
    <duel>
      <server>{duel.server}</server>
      <map>{duel.map}</map>
      <timestamp>{duel.timestamp}</timestamp>
      <mode>{duel.mode}</mode>{
      <duration>{cw.gameDuration}</duration>
      val sortedByScore = duel.players.mapValues{_.frags}.toList.sortBy{_._2}
      if ( sortedByScore.map{_._2}.toSet.size > 1 ) {
        val (name, winner) = sortedByScore.last
        <winner>
          {name}
        </winner>
      }
      }
      {
        for {
          (name, player) <- duel.players
        } yield <player>
        <name>{name}</name>
          <weapon>{player.mainWeapon}</weapon>
          <frags>{player.frags}</frags>
          <ip>{player.ip}</ip>
        </player>
      }
    </duel>
  }


}
