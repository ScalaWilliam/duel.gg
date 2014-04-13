package us.woop.pinger.analytics.processing

import scalaz.stream._
import scalaz.stream.Process._
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.ParsedTypedMessageConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import scalaz.stream.Process.Emit
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import scala.Some
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessage
import org.joda.time.format.ISODateTimeFormat
import scala.xml.Elem
import us.woop.pinger.analytics.data.{GameData, ModesList}

object Collector {

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  /*** From a sequence of ParsedMessage extract individual games into self contained units .!. ***/
  def getGame[F]: scalaz.stream.Process.Process1[ParsedMessage, GameData] = {
    def go(gameParsed: Option[ParsedMessage], game: Option[ParsedTypedMessage[ConvertedServerInfoReply]], acc: List[ParsedMessage]): scalaz.stream.Process.Process1[ParsedMessage, GameData] = {
      game match {
        case Some(activeGame) =>
          Process.await1[ParsedMessage].flatMap{
            case x @ ParsedTypedMessageConvertedServerInfoReply(f) if isSwitch(activeGame.message, f.message) =>
              val gd = GameData(firstTime = activeGame, nextGame = Option(f), data = acc)
              Emit(Seq(gd), go(Option(x), Option(f), List(x)))
            case g =>
              go(gameParsed, game, acc :+ g)
          } orElse Emit(Seq(GameData(firstTime = activeGame, nextGame = None, data = acc)), halt)
        case None =>
          process1.dropWhile[ParsedMessage]{ x => !x.message.isInstanceOf[ConvertedServerInfoReply]}.flatMap{
            case x@ParsedTypedMessageConvertedServerInfoReply(f) =>
              go(Option(x), Option(f), List(x))
          } orElse halt
      }
    }
    go(None, None, List.empty)
  }

  import scalaz.stream.Process.Process1
  def processGame: Process1[GameData, Elem] = scalaz.stream.process1.lift {
    x =>
      val duel = DuelMaker.makeDuel(x)
      val cm = ClanmatchMaker.makeMatch(x)

      val xmlResult = List(duel, cm).collectFirst{
        case Right(goodGame) => goodGame
      }.getOrElse {
        <othergame>
          <server>{x.firstTime.server.ip.ip}:{x.firstTime.server.port}</server>
          <map>{x.firstTime.message.mapname}</map>
          <timestamp>{ISODateTimeFormat.dateTimeNoMillis().print(x.firstTime.time)}</timestamp>
          {x.firstTime.message.gamemode.flatMap(ModesList.modes.get).toSeq.map{mode => <mode>{mode.name}</mode>}}
          <failures>
            <duel>{duel.left.get}</duel>
            <clanmatch>{cm.left.get}</clanmatch>
          </failures>
        </othergame>
      }
      xmlResult
  }

}
