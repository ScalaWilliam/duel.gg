package us.woop.pinger.analytics.processing

import org.joda.time.format.ISODateTimeFormat
import us.woop.pinger.client.PingPongProcessor
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessage
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.{ParsedTypedMessageConvertedServerInfoReply, ParsedTypedMessagePlayerExtInfo}
import PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import scala.util.Try
import us.woop.pinger.analytics.data.ModesList
import ModesList.Weapon
import us.woop.pinger.analytics.data.GameData

object DuelMaker {

  private def meaningfulDuration(seconds: Int): String = {
    val minutes = meaningfulMinutes(seconds)
    s"$minutes minutes"
  }
  private def meaningfulMinutes(seconds: Int): Int = {
    Math.round(seconds.toDouble / 60).toInt
  }

  private case class Duel(timestamp: String, server: Server, map: String, mode: String, winner: Option[String], gameDuration: Int, players: Map[String, Player], playing: Boolean, activeAt: List[Int])

  private case class Player(name: String, ip: String, fragsLog: Map[Int, Int], weaponsLog: Map[Int, Int], frags: Int, mainWeapon: Int)

  private type PotentialGame = Either[String, Duel]
  def makeDuel(gameData: GameData) = {

    val initialDuel: PotentialGame = for {
      cw <- Right{
        import gameData.firstTime._
        import message._
        Duel(timestamp = ISODateTimeFormat.dateTimeNoMillis().print(gameData.firstTime.time), server = server, map = mapname, mode = gamemode.map {
          _.toString
        }.getOrElse(""), winner = None, gameDuration = 0, players = Map.empty, playing = true, activeAt = List.empty)
      }.right

      gameMode <- Try(cw.mode.toInt).map{Right(_)}.getOrElse{Left(s"Unknown mode: ${cw.mode}")}.right
      modeDetail <- (ModesList.modes.get(gameMode) match {
        case Some(mode) => Right(mode)
        case None => Left(s"Mode $gameMode not found")
      }).right
      allowedMode <- (if (!modeDetail.keys.contains(ModesList.ModeParams.M_TEAM)) Right(true) else Left(s"Mode not a teammode")).right
    } yield cw.copy(mode = modeDetail.name)

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

    val duelModes = List(1,4,6)
    val duelCalculation = gameData.gameMessages.foldLeft[PotentialGame](initialDuel){

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
          val newFragsLog = player.fragsLog.updated(meaningfulMinutes(duel.gameDuration), info.frags)
          if ( info.teamkills > 0  ) {
            println("OK")
          }
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
      case (Right(duel), _ ) => Right(duel)
    }

    def gameMatching(game: Duel): PotentialGame =
      if ( game.gameDuration > 280) Right(game) else Left(s"Game only lasted ~${game.gameDuration} seconds")

    for {
      duel <- duelCalculation.right
      _ <- gameMatching(duel).right
      _ <- (if (duel.players.size < 2) Left(s"Not enough players") else Right(duel)).right
    } yield
    <duel>
      <server>{duel.server.ip.ip}:{duel.server.port}</server>
      <map>{duel.map}</map>
      <timestamp>{duel.timestamp}</timestamp>
      <mode>{duel.mode}</mode>
      <duration>{meaningfulDuration(duel.gameDuration)}</duration>
      {
      val sortedByScore = duel.players.mapValues{_.frags}.toList.sortBy{_._2}
      if ( sortedByScore.map{_._2}.toSet.size > 1 ) {
        val (name, winner) = sortedByScore.last
        <winner>{name}</winner>
      }
      }
      {
        for {
          (name, player) <- duel.players
        } yield <player>
        <name>{name}</name>
          {Weapon(player.mainWeapon).xml}
          <frags>{player.frags}</frags>
          <ip>{player.ip}</ip>
          {
          List(player.fragsLog).filter{_.nonEmpty}.map{sl =>
            <log>{
              val gameMinutes = meaningfulMinutes(duel.gameDuration)
            (1 to gameMinutes).flatMap{ min => sl.get(min).toList.map{min -> _}}.map {
              case (y, x) => s"""$y:$x"""
            }.mkString(",")}</log>}}
        </player>
      }
    </duel>
  }


}
