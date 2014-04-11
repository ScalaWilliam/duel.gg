package us.woop.pinger.data

import PingPongProcessor.Server

/** Parse stuff into meaningful stuff and send out to all subscribers **/
object ParsedProcessor {
  case object Subscribe
  case object Unsubscribe
  case class ParsedMessage(server: Server, time: Long, message: Any)
  case class ParsedTypedMessage[T](server: Server, time: Long, message: T)
  import scala.reflect.runtime.universe._
  abstract class ParsedTypedMessageConversion[T](implicit tt: TypeTag[T]) {
    def getType(message: Any) = runtimeMirror(getClass.getClassLoader).classSymbol(message.getClass).toType
    def unapply(m: ParsedMessage): Option[ParsedTypedMessage[T]] = {
      Option(m).collect {
        case ParsedMessage(server, time, message) if getType(message) =:= typeOf[T] =>
          ParsedTypedMessage(server, time, message.asInstanceOf[T])
      }
    }
  }

  import us.woop.pinger.SauerbratenServerData.Conversions
  import us.woop.pinger.SauerbratenServerData

  object ParsedTypedMessages {
    object ParsedTypedMessageConvertedServerInfoReply extends ParsedTypedMessageConversion[Conversions.ConvertedServerInfoReply]
    object ParsedTypedMessageConvertedHopmodUptime extends ParsedTypedMessageConversion[Conversions.ConvertedHopmodUptime]
    object ParsedTypedMessageConvertedTeamScore extends ParsedTypedMessageConversion[Conversions.ConvertedTeamScore]
    object ParsedTypedMessageConvertedThomasExt extends ParsedTypedMessageConversion[Conversions.ConvertedThomasExt]
    object ParsedTypedMessageServerInfoReply extends ParsedTypedMessageConversion[SauerbratenServerData.ServerInfoReply]
    object ParsedTypedMessageHopmodUptime extends ParsedTypedMessageConversion[SauerbratenServerData.HopmodUptime]
    object ParsedTypedMessageOlderClient extends ParsedTypedMessageConversion[SauerbratenServerData.OlderClient]
    object ParsedTypedMessagePlayerCns extends ParsedTypedMessageConversion[SauerbratenServerData.PlayerCns]
    object ParsedTypedMessagePlayerExtInfo extends ParsedTypedMessageConversion[SauerbratenServerData.PlayerExtInfo]
    object ParsedTypedMessageTeamScores extends ParsedTypedMessageConversion[SauerbratenServerData.TeamScores]
    object ParsedTypedMessageThomasExt extends ParsedTypedMessageConversion[SauerbratenServerData.ThomasExt]
    object ParsedTypedMessageUptime extends ParsedTypedMessageConversion[SauerbratenServerData.Uptime]
  }
}
