package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.MIteratorState.MInitial
import gg.duel.pinger.data.ParsedPongs.ParsedMessage
import gg.duel.pinger.data.{Extractor, SauerBytes}

import scala.util.control.NonFatal

object MultiplexedReader {

  def sauerBytesToParsedMessages(sauerBytes: SauerBytes): Option[ParsedMessage] = {
    val result = try {
      Extractor
        .extractDuel
        .lift(sauerBytes.message)
        .map(parsedObject => ParsedMessage(sauerBytes.server, sauerBytes.time, parsedObject))
    } catch {
      case NonFatal(e) => Option.empty
    }
    result
  }

  def multiplexParsedMessagesStates(parsedMessages: Iterator[ParsedMessage]): Iterator[MIteratorState] = {
    parsedMessages.scanLeft(MInitial: MIteratorState)(_.next(_))
  }
}




