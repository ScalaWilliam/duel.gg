package gg.duel.tourney

import scala.xml.Elem

object SingleElimination {

  sealed trait GameResult
  case class GameWon(winner: String) extends GameResult
  case class GameFailed(reason: String) extends GameResult

  sealed trait GameStatus
  case object GameNotReady extends GameStatus
  case class GameStarted(start: Int, deadline: Int) extends GameStatus
  case class GameFinished(gameStarted: GameStarted, atTime: Int, result: GameResult) extends GameStatus
  case class Players(firstPlayer: Option[String], secondPlayer: Option[String])
  object Players {
    def none: Option[Players] = None
    def apply(first: String, second: String): Option[Players] = Option(Players(Option(first), Option(second)))
  }
  case class Game(gameStatus: GameStatus, players: Option[Players])

  object SingleEliminationTournament {
    def startFour(defaultDeadline: Int, players: List[String]) = {
      val games = Map(
        1 -> Game(GameStarted(0, 10), Players(players(0), players(1))),
        2 -> Game(GameStarted(0, 10), Players(players(2), players(3))),
        3 -> Game(GameNotReady, Players.none)
      )
      val winnerFlow = List(1 -> 3, 2 -> 3)
      SingleEliminationTournament(defaultDeadline, players, games, winnerFlow)
    }
    def startEight(defaultDeadline: Int, players: List[String]) = {
      val games = Map(
        1 -> Game(GameStarted(0, 10), Players(players(0), players(1))),
        2 -> Game(GameStarted(0, 10), Players(players(2), players(3))),
        3 -> Game(GameStarted(0, 10), Players(players(4), players(5))),
        4 -> Game(GameStarted(0, 10), Players(players(6), players(7))),
        5 -> Game(GameNotReady, None),
        6 -> Game(GameNotReady, None),
        7 -> Game(GameNotReady, None)
      )
      val winnerFlow = List(1 -> 5, 2 -> 5, 3 -> 6, 4 -> 6, 5 -> 7, 6 -> 7)
      SingleEliminationTournament(defaultDeadline, players, games, winnerFlow)
    }
    def startSixteen(defaultDeadline: Int, players: List[String]) = {
      val games = Map(
         1 -> Game(GameStarted(0, 10), Players(players( 0), players( 1))),
         2 -> Game(GameStarted(0, 10), Players(players( 2), players( 3))),
         3 -> Game(GameStarted(0, 10), Players(players( 4), players( 5))),
         4 -> Game(GameStarted(0, 10), Players(players( 6), players( 7))),
         5 -> Game(GameStarted(0, 10), Players(players( 8), players( 9))),
         6 -> Game(GameStarted(0, 10), Players(players(10), players(11))),
         7 -> Game(GameStarted(0, 10), Players(players(12), players(13))),
         8 -> Game(GameStarted(0, 10), Players(players(14), players(15))),
         9 -> Game(GameNotReady, None),
        10 -> Game(GameNotReady, None),
        11 -> Game(GameNotReady, None),
        12 -> Game(GameNotReady, None),
        13 -> Game(GameNotReady, None),
        14 -> Game(GameNotReady, None),
        15 -> Game(GameNotReady, None)
      )
      val winnerFlow = List(
         1 ->  9,  2 ->  9,
         3 -> 10,  4 -> 10,
         5 -> 11,  6 -> 11,
         7 -> 12,  8 -> 12,
         9 -> 13, 10 -> 13,
        11 -> 14, 12 -> 14,
        13 -> 15, 14 -> 15
      )
      SingleEliminationTournament(defaultDeadline, players, games, winnerFlow)
    }
  }

  case class SingleEliminationTournament(normalDeadline: Int, players: List[String], games: Map[Int, Game], winnerFlow: List[(Int, Int)], currentTime: Int = 0) {

    def activeGames: List[(Int, Game)] = {
      for { (gameId, game @ Game(GameStarted(_, _), _)) <- games.toList }
      yield gameId -> game
    }

    def flowTriples: List[(Int, Game, Int, Game, Int, Game)] =
      for {
        (firstGameId, targetGameId) <- winnerFlow
        (secondGameId, `targetGameId`) <- winnerFlow
        if firstGameId < secondGameId
        (`firstGameId`, firstGame) <- games
        (`secondGameId`, secondGame) <- games
        (`targetGameId`, targetGame) <- games
      } yield (targetGameId, targetGame, firstGameId, firstGame, secondGameId, secondGame)

    def gameDependants: List[(Int, Game, List[(Int, Game)])] = {
      for { (nid, ng, fid, fg, sid, sg) <- flowTriples }
        yield (nid, ng, List(fid -> fg, sid -> sg))
    }

    private def iterateMissedDeadlines: SingleEliminationTournament = {
      val updatedGames = for {
        (gameId, game @ Game(gameStarted @ GameStarted(start, deadline), _)) <- games
        if currentTime > deadline
      } yield gameId -> game.copy(GameFinished(gameStarted, currentTime, GameFailed(s"deadline of $deadline missed, now $currentTime")))
      copy(games = games ++ updatedGames)
    }

    private def iterateTwoFailedSiblings: SingleEliminationTournament = {
      val updatedGames = for {
        (
          nextGameId, nextGame @ Game(GameNotReady, _),
          firstId, Game(GameFinished(_, _, GameFailed(firstReason)), _),
          secondId, Game(GameFinished(_, _, GameFailed(secondReason)), _)
        ) <- flowTriples
      } yield nextGameId -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameFailed(s"both dependant games ($firstId and $secondId) failed due to: $firstReason, $secondReason")))
      copy(games = games ++ updatedGames)
    }

    private def iterateOnePendingOneWinSibling: SingleEliminationTournament = {
      val updatedGames = for {
        (nextGameId, nextGame @ Game(GameNotReady, _), sourceGames) <- gameDependants
        (winningGameId, Game(GameFinished(_, _, GameWon(winner)), _)) <- sourceGames
        (pendingGameId, pendingGame) <- sourceGames
        if !pendingGame.gameStatus.isInstanceOf[GameFinished]
        gamePlayers = if ( winningGameId < pendingGameId ) Players(Option(winner), None ) else Players(None, Option(winner))
      } yield nextGameId -> nextGame.copy(players = Option(gamePlayers))
      copy(games = games ++ updatedGames)
    }

    private def iterateOneFailOneWinSibling: SingleEliminationTournament = {
      val updatedGames = for {
        (nextGameId, nextGame @ Game(GameNotReady, _), sourceGames) <- gameDependants
        (winningGameId, Game(GameFinished(_, _, GameWon(winner)), _)) <- sourceGames
        (failedGameId, Game(GameFinished(_, _, GameFailed(reason)), _)) <- sourceGames
        gamePlayers = if ( winningGameId < failedGameId ) Players(Option(winner), None ) else Players(None, Option(winner))
      } yield nextGameId -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(winner)), players = Option(gamePlayers))
      copy(games = games ++ updatedGames)
    }

    private def iterateTwoWinningSiblings: SingleEliminationTournament = {
      val updatedGames = for {
        (nextGameId, nextGame @ Game(GameNotReady, _), firstGameId, Game(GameFinished(_, _, GameWon(firstWinner)),_), secondGameId, Game(GameFinished(_, _, GameWon(secondWinner)), _)) <- flowTriples
      } yield nextGameId -> nextGame.copy(GameStarted(currentTime, currentTime + normalDeadline), players = Players(firstWinner, secondWinner))
      copy(games = games ++ updatedGames)
    }

    def synced: SingleEliminationTournament = {
      def go(start: SingleEliminationTournament): SingleEliminationTournament = {
        start.iterateTwoFailedSiblings match {
          case `start` => start
          case other => go(other)
        }
      }
      go(this.iterateMissedDeadlines).iterateOneFailOneWinSibling
    }

    def withTime(newTime: Int): SingleEliminationTournament = {
      copy(currentTime = newTime).synced
    }

    def withFailedGame(gameId: Int, reason: String): SingleEliminationTournament = {
      val updatedGames =
        for {
          (`gameId`, game) <- games
        } yield (gameId, game match {
          case Game(started: GameStarted, _) => game.copy(GameFinished(started, currentTime, GameFailed(reason)))
          case other => other.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameFailed(reason)))
        })
      copy(games = games ++ updatedGames).synced
    }

    def withWonGame(gameId: Int, winner: String): SingleEliminationTournament = {
      val updatedGames =
        for {
          (`gameId`, game) <- games
        } yield (gameId, game match {
          case Game(started: GameStarted, _) => game.copy(GameFinished(started, currentTime, GameWon(winner)))
          case other => other.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(winner)))
        })
      copy(games = games ++ updatedGames).iterateTwoWinningSiblings
    }

  }

}


object MoreTest extends App {
  //  val tourney = SingleElimination.SingleEliminationTournament.startFour(List("John", "Betsy", "Peter", "Kate"))
  //  println(tourney)
  //  println(tourney.withTime(10))
  //  println(tourney.withTime(10).withTime(10))
  //  println(tourney.withTime(10).withTime(11))
  //  val tourney = SingleElimination.SingleEliminationTournament.startEight(List("John", "Betsy", "Peter", "Kate", "Robert", "Janice", "Edith", "Max"))
  //  println(tourney)
  //  println(tourney.withTime(10))
  //  println(tourney.withTime(10).withTime(10))
  //  println(tourney.withTime(10).withTime(11))


  val tourney =
    SingleElimination.SingleEliminationTournament
      .startEight(10, List("John", "Betsy", "Peter", "Kate", "Robert", "Janice", "Edith", "Max"))
      .withTime(6)
      .withWonGame(1, "John")
      .withWonGame(2, "Peter")
      .withWonGame(4, "Max")
      .withTime(11)
      .withWonGame(5, "John")
      .withTime(20)
      .withWonGame(7, "John")
  //      .withFailedGame(5, "no game played")
  //      .withWonGame(5, "John")
  //      .withTime(22)

  tourney.games.toList.sortBy(_._1) foreach println

  def printTourney(tourney: SingleElimination.SingleEliminationTournament) = {
    import SingleElimination._
    def printBracket(gameId: Int): Seq[Elem] = for {
      (`gameId`, game) <- tourney.games.toSeq
      depGames = tourney.winnerFlow.filter(_._2 == gameId).map(_._1)
    } yield <game id={gameId.toString}>
      <content>{s"$gameId: $game"}</content>

        {
        for {
          List(leftId, rightId) <- Option(depGames).toList
          g <- Seq(
            <left game-id={leftId.toString}>{printBracket(leftId).toSeq}</left>,
            <right game-id={rightId.toString}>{printBracket(rightId).toSeq}</right>
          )
        } yield g }
        </game>
    printBracket(7)
  }

  println(printTourney(tourney))

}