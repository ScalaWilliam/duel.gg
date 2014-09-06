package us.woop.pinger.analytics

import org.scalatest.{Inspectors, Matchers, WordSpec}
import us.woop.pinger.analytics.MultiplexedDuelReader.MFoundGame
import us.woop.pinger.data.ParsedPongs.{ParsedMessage, PlayerExtInfo}
import us.woop.pinger.data.Server

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
      import us.woop.pinger.analytics.stub.StubGenerator._
      val firstServerSequence = itemsToList()(validSequence :_*)

      val secondServerSequence = {
        val seqA = itemsToList()(validSequence :_*).map(_.copy(server = Server("32.1.1.1", 1222)))
        val secondLast = seqA(seqA.length - 2)
        seqA.updated(
          seqA.length - 2,
          secondLast.copy(message = secondLast.message.asInstanceOf[PlayerExtInfo].copy(frags = 9)): ParsedMessage
        )
      }

//      secondServerSequence foreach println

      val haveThis = randomlyInterleave(Nil, firstServerSequence, secondServerSequence)

      val gotStates = timedMultiplexedStates(haveThis).toList

      forExactly(2, gotStates) {
        _ shouldBe a [MFoundGame]
      }

      val foundGames = gotStates.collect{
        case MFoundGame(_, game) => game.toSimpleCompletedDuel
      }
//      foundGames foreach println
      forExactly(1, foundGames) {
        game =>
          game.server shouldBe "123.2.2.22:2134"
          game.winner shouldBe Some("w00p|Drakas")
          game.players("w00p|Drakas").frags shouldBe 4
          game.players("w00p|Art").accuracy shouldBe 25
      }

      // found a bug through here. YAY!
      forExactly(1, foundGames) {
        game =>
          game.server shouldBe "32.1.1.1:1222"
          game.winner shouldBe Some("w00p|Art")
          game.players("w00p|Art").frags shouldBe 9
          game.players("w00p|Art").accuracy shouldBe 25
      }
    }
  }
}
