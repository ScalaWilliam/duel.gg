package gg.duel

import gg.duel.query.GameId
import org.scalatest.{Matchers, Inside, OptionValues, WordSpec}

import scala.concurrent.{ExecutionContext, Future}

class FilteringSpec
  extends WordSpec
  with Inside
  with OptionValues
  with Matchers
{

  val games = List(
    SimpleGame.stub(id = "a"),
    SimpleGame.stub(id = "b"),
    SimpleGame.stub(id = "c"),
    SimpleGame.stub(id = "d"),
    SimpleGame.stub(id = "e"),
    SimpleGame.stub(id = "f")
  )

  object SingleFilter {
    def apply(gameId: GameId): Option[SingleFilteringResult] = {
      val sorted = games.sortBy(_.id)
      PartialFunction.condOpt(sorted.indexWhere(_.id == gameId.gameId)) {
        case n if n >= 0 =>
          val nextGame = sorted.lift.apply(n+1)
          val previousGame = sorted.lift.apply(n-1)
          val currentGame = sorted(n)
          SingleFilteringResult(
            nextGame = nextGame.map(_.id).map(GameId.apply),
            previousGame = previousGame.map(_.id).map(GameId.apply),
            game = currentGame
          )
      }
    }
  }

  case class SingleFilteringResult
  (previousGame: Option[GameId],
    nextGame: Option[GameId],
    game: SimpleGame)

  "Filterer" must {
    "Work properly" in {
      inside(SingleFilter.apply(GameId("a")).value) {
        case SingleFilteringResult(previous, next, game) =>
          previous shouldBe empty
          next.value shouldBe GameId("b")
          game.id shouldBe "a"
      }
    }
  }


}