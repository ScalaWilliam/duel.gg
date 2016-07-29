package gg.duel.pinger.analytics.duel

import gg.duel.pinger.analytics.duel.ZIteratorState.ZOutOfGameState
import gg.duel.pinger.data.ParsedPongs.ParsedMessage

object StreamedSimpleDuelMaker {

  def parsedToState(iterator: Iterator[ParsedMessage]): Iterator[ZIteratorState] =
    iterator.scanLeft(ZOutOfGameState: ZIteratorState)(_.next(_))

  object IteratorImplicit {

    implicit class onIterator(iterator: Iterator[ParsedMessage]) {
      def asStateIterator = parsedToState(iterator)
    }

  }

}
