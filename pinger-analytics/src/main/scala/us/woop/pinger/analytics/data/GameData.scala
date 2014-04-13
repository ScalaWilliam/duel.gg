package us.woop.pinger.analytics.data

import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessage
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.ParsedMessage

case class GameData(firstTime: ParsedTypedMessage[ConvertedServerInfoReply], nextGame: Option[ParsedTypedMessage[ConvertedServerInfoReply]], data: List[ParsedMessage])
