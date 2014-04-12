package pinger

import us.woop.pinger.data.actor.ParsedProcessor
import ParsedProcessor.{ParsedTypedMessageConversion, ParsedTypedMessage}
import us.woop.pinger.Collector.GameData
import scalaz.stream._
import scalaz.stream.Process._
import ParsedProcessor.ParsedTypedMessages.{ParsedTypedMessageConvertedServerInfoReply, ParsedTypedMessagePlayerExtInfo}
import scala.annotation.tailrec
import org.joda.time.format.ISODateTimeFormat

object DuelMaker {

  sealed trait GameActiveStatus { def from: Int; def to: Int }
  case class GameRunning(from: Int, to: Int) extends GameActiveStatus
  case class GamePaused(from: Int, to: Int) extends GameActiveStatus
  case class Player(name: String, ip: String)
  case class PlayState(time: Int, gun: Int, frags: Int)

  def makeDuel(gameData: GameData) = {

    val startTime = gameData.firstTime.time

    val finishTime = gameData.data.reverse.collectFirst {
      case ParsedTypedMessageConvertedServerInfoReply(ParsedTypedMessage(server, mtime, info)) =>
        mtime
    }.getOrElse(startTime)

    assert(finishTime - startTime > 299000, s"Game must be a full game")

    val playerStates = gameData.data.collect {
      case ParsedTypedMessagePlayerExtInfo(ParsedTypedMessage(server, mtime, playerExtInfo))
      if (0 to 4) contains playerExtInfo.state =>
        Player(playerExtInfo.name,playerExtInfo.ip) -> PlayState(((mtime - startTime) / 1000).toInt, playerExtInfo.gun, playerExtInfo.frags)
    }.groupBy{_._1}.mapValues{_.map{_._2}}

    assert(playerStates.size == 2, s"Player states size = ${playerStates.size}")

    val calculatedPauses = {
      val activeStates = gameData.data.collect {
        case ParsedTypedMessageConvertedServerInfoReply(ParsedTypedMessage(server, mtime, info)) =>
          val time = ((mtime - startTime) / 1000).toInt
          if (info.gamepaused) GamePaused(time, time) else GameRunning(time, time)
      }
      @tailrec
      def calculatePausesActivities(left: List[GameActiveStatus]): List[GameActiveStatus] = {
        val reduced = left.sliding(2).flatMap {
          case GamePaused(a, b) :: GamePaused(c, d) :: Nil => GamePaused(a, b) :: Nil
          case GameRunning(a, b) :: GameRunning(c, d) :: Nil => GameRunning(a, d) :: Nil
          case other => other
        }.toList
        reduced.length match {
          case x if x == left.length => reduced
          case other => calculatePausesActivities(reduced)
        }
      }
      calculatePausesActivities(activeStates)
    }

    val he = playerStates.mapValues{x => x.map{_.gun}.filterNot{_==6}.groupBy{identity}.mapValues{_.length}.toList.sortBy{_._2}.reverse.map{_._1}.head}

    val finalResult = playerStates.mapValues{_.last.frags}

    val timeline = playerStates.mapValues{ items =>
      for {
        minute <- 1 to (gameData.firstTime.message.remain / 60)
        value = items.collectFirst{case x if x.time > minute * 60 => x.frags}
      } yield value
    }

    val winner = finalResult.maxBy{_._2}._1
      <duel>
        <server>{gameData.firstTime.server.ip}:{gameData.firstTime.server.port}</server>
        <map>{gameData.firstTime.message.mapname}</map>
        <datetime>{ISODateTimeFormat.dateTimeNoMillis().print(startTime)}</datetime>
        <mode>{gameData.firstTime.message.gamemode.getOrElse("")}</mode>
        <winner>
          <ip>{winner.ip}</ip>
          <name>{winner.name}</name>
        </winner>
        {
        for {
          (id, result) <- finalResult
          weapon <- he get id
          times <- timeline get id
        }
        yield <player>
          <ip>{id.ip}</ip>
          <name>{id.name}</name>
          <frags>{result}</frags>
          <weapon>{weapon}</weapon>
          <timeline>{times map {_.getOrElse("")} mkString ","}</timeline>
        </player>
        }
      </duel>
  }

}
