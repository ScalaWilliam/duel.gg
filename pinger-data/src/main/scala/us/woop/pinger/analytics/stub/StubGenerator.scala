package us.woop.pinger.analytics.stub

import us.woop.pinger.analytics.MultiplexedDuelReader.MIteratorState
import us.woop.pinger.analytics.{MultiplexedDuelReader, StreamedDuelMaker}
import us.woop.pinger.analytics.StreamedDuelMaker.{ZFoundGame, ZIteratorState}
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.{ParsedMessage, PlayerExtInfo}
import us.woop.pinger.data.Server

object StubGenerator {

  def pei(name: String, frags: Int, ip: String) = {
    PlayerExtInfo(0, 0, 0, name, "", frags, 0, 0, 25, 100, 0, 1, 0, 1, ip)
  }

  def csr(clients: Int, mode: Int, remain: Int, mapname: String) = {
    ConvertedServerInfoReply(
      clients, 0, mode, remain, 10, false, 100, mapname, "test"
    )
  }

  def states(items: Any*): List[ZIteratorState] = {
    val server = Server("123.2.2.22", 2134)
    val time = 12312412L
    StreamedDuelMaker.parsedToState(
      items.zipWithIndex.map{case (item, n) => ParsedMessage(server, time + n, item)}.toIterator
    ).toList
  }

  def timedStates(items: (Int, Any)*): List[ZIteratorState] = {
    val server = Server("123.2.2.22", 2134)
    val time = 12312412L
    StreamedDuelMaker.parsedToState(
      items.map(_.swap).map{case (item, n) => ParsedMessage(server, time + n * 1000, item)}.toIterator
    ).toList
  }

  def itemsToList(server: Server = Server("123.2.2.22", 2134), time:Long = 123123123L)(items: (Int, Any)*): List[ParsedMessage] = {
    items.map(_.swap).map{case (item, n) => ParsedMessage(server, time + n * 1000, item)}.toList
  }


  def timedMultiplexedStates(items: List[ParsedMessage]): List[MIteratorState] = {
    MultiplexedDuelReader.multiplexParsedMessagesStates(
      items.toIterator
    ).toList
  }

  // minimum viable sequence to produce a completed duel!
  val validSequence = Seq(
    0 * 60 -> csr(2, 3, 600 - (0 * 60), "academy"),
    // state above 3, meaning spectator, for example
    1 * 60 -> pei("w00p|Drakas", 2, "123"),
    1 * 60 -> pei("w00p|Art", 2, "123"),
    1 * 60 -> pei("w00p|brownie", 2, "123").copy(state = 4),
    1 * 60 -> csr(2, 3, 600 - (1 * 60), "academy"),
    2 * 60 -> csr(4, 3, 600 - (2 * 60), "academy"),
    3 * 60 -> csr(6, 3, 600 - (3 * 60), "academy"),
    4 * 60 -> csr(2, 3, 600 - (4 * 60), "academy"),
    5 * 60 -> csr(9, 3, 600 - (5 * 60), "academy"),
    6 * 60 -> csr(2, 3, 600 - (6 * 60), "academy"),
    7 * 60 -> csr(2, 3, 600 - (7 * 60), "academy"),
    8 * 60 -> csr(2, 3, 600 - (8 * 60), "academy"),
    8 * 60 -> pei("w00p|Drakas", 4, "123"),
    8 * 60 -> pei("w00p|Art", 3, "123"),
    4 * 60 -> csr(2, 2, 500, "tartech")
  )

  val validSequenceCompletedDuel = timedStates(validSequence :_*).last.asInstanceOf[ZFoundGame].completedDuel
  
}
