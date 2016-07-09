package gg.duel.pinger.analytics

import gg.duel.pinger.analytics.MultiplexedReader.{MIteratorState, CompletedGame, MFoundGame}
import gg.duel.pinger.analytics.duel.StubGenerator
import org.scalatest.{Inspectors, Matchers, WordSpec}
import gg.duel.pinger.data.ParsedPongs.{ParsedMessage, PlayerExtInfo}
import gg.duel.pinger.data.Server

import scala.annotation.tailrec

class MultiplexedDuelReaderSpec extends WordSpec with Matchers with Inspectors {
  val random = new scala.util.Random()
  @tailrec
  final def randomlyInterleave[T](accum: List[T], a: List[T], b: List[T]): List[T] = {
    if ( a.isEmpty && b.isEmpty ) {
      accum
    } else if ( a.isEmpty ) {
      accum ::: b
    } else if ( b.isEmpty ) {
      accum ::: a
    } else if ( random.nextBoolean() ) {
      randomlyInterleave(accum :+ a.head, a.tail, b)
    } else {
      randomlyInterleave(accum :+ b.head, a, b.tail)
    }
  }
  "Multiplexer" must {
    "Parse two interleaved results correctly" in {
      import StubGenerator._
      val firstServerSequence = itemsToList()(validSequence :_*)

      val secondServerSequence = {
        val seqA = itemsToList()(validSequence :_*).map(_.copy(server = Server("32.1.1.1", 1222)))
        val secondLast = seqA(seqA.length - 2)
        seqA.updated(
          seqA.length - 2,
          secondLast.copy(message = secondLast.message.asInstanceOf[PlayerExtInfo].copy(frags = 21)): ParsedMessage
        )
      }

//      secondServerSequence foreach println

      val haveThis = randomlyInterleave(Nil, firstServerSequence, secondServerSequence)

      def timedMultiplexedStates(items: List[ParsedMessage]): List[MIteratorState] = {
        MultiplexedReader.multiplexParsedMessagesStates(
          items.toIterator
        ).toList
      }

      val gotStates = timedMultiplexedStates(haveThis)

      forExactly(2, gotStates) {
        _ shouldBe a [MFoundGame]
      }

      val foundGames = gotStates.collect{
        case MFoundGame(_, CompletedGame(Left(game),_),_) => game
      }
//      foundGames foreach println
      forExactly(1, foundGames) {
        game =>
          game.server shouldBe "123.2.2.22:2134"
          game.winner shouldBe Some("w00p|Drakas")
          game.players("w00p|Drakas").frags shouldBe 40
          game.players("w00p|Art").accuracy shouldBe 25
      }

      // found a bug through here. YAY!
      forExactly(1, foundGames) {
        game =>
          game.server shouldBe "32.1.1.1:1222"
          game.winner shouldBe Some("w00p|Drakas")
          game.players("w00p|Art").frags shouldBe 21
          game.players("w00p|Art").accuracy shouldBe 25
      }
    }
  }
}
