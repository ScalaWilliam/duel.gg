package us.woop.pinger

import scalaz.stream._
import scalaz.stream.Process._
import us.woop.pinger.data.ParsedProcessor.{ParsedTypedMessage, ParsedMessage}
import us.woop.pinger.SauerbratenServerData.Conversions.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedProcessor.ParsedTypedMessages.ParsedTypedMessageConvertedServerInfoReply

object Collector {

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  case class GameData(firstTime: ParsedTypedMessage[ConvertedServerInfoReply], nextGame: Option[ParsedTypedMessage[ConvertedServerInfoReply]], data: List[ParsedMessage])

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
}
