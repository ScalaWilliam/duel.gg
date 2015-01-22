package gg.duel.tourney

object DoubleElimination {

  sealed trait GameResult
  case class GameWon(winner: String, loser: Option[String]) extends GameResult
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
  case class Game(gameStatus: GameStatus, players: Option[Players], isWinning: Boolean) {
    def playersList = players.toList.flatMap(p => p.firstPlayer.toList ++ p.secondPlayer.toList)
    def started(atTime: Int, deadline: Int, first: String, second: String) = {
      copy(GameStarted(atTime, deadline), Players(first, second))
    }
  }

  object DoubleEliminationTournament {
    def startFour(defaultDeadline: Int, players: List[String]) = {
      val games = Map(
        1 -> Game(GameStarted(0, defaultDeadline), Players(players(0), players(1)), isWinning = true),
        2 -> Game(GameStarted(0, defaultDeadline), Players(players(2), players(3)), isWinning = true),
        3 -> Game(GameNotReady, Players.none, isWinning = true),
        4 -> Game(GameNotReady, Players.none, isWinning = false),
        5 -> Game(GameNotReady, Players.none, isWinning = false),
        6 -> Game(GameNotReady, Players.none, isWinning = true)
      )
      val winnerFlow = List(1 -> 3, 2 -> 3, 3 -> 6, 4 -> 5, 5 -> 6)
      val loserFlow = List(1 -> 4, 2 -> 4, 3 -> 5)
      DoubleEliminationTournament(defaultDeadline, players, games, winnerFlow, loserFlow)
    }
  }
//    def startEight(defaultDeadline: Int, players: List[String]) = {
//      val games = Map(
//        1 -> Game(GameStarted(0, 10), Players(players(0), players(1))),
//        2 -> Game(GameStarted(0, 10), Players(players(2), players(3))),
//        3 -> Game(GameStarted(0, 10), Players(players(4), players(5))),
//        4 -> Game(GameStarted(0, 10), Players(players(6), players(7))),
//        5 -> Game(GameNotReady, None),
//        6 -> Game(GameNotReady, None),
//        7 -> Game(GameNotReady, None)
//      )
//      val winnerFlow = List(1 -> 5, 2 -> 5, 3 -> 6, 4 -> 6, 5 -> 7, 6 -> 7)
//      SingleEliminationTournament(defaultDeadline, players, games, winnerFlow)
//    }
//    def startSixteen(defaultDeadline: Int, players: List[String]) = {
//      val games = Map(
//        1 -> Game(GameStarted(0, 10), Players(players( 0), players( 1))),
//        2 -> Game(GameStarted(0, 10), Players(players( 2), players( 3))),
//        3 -> Game(GameStarted(0, 10), Players(players( 4), players( 5))),
//        4 -> Game(GameStarted(0, 10), Players(players( 6), players( 7))),
//        5 -> Game(GameStarted(0, 10), Players(players( 8), players( 9))),
//        6 -> Game(GameStarted(0, 10), Players(players(10), players(11))),
//        7 -> Game(GameStarted(0, 10), Players(players(12), players(13))),
//        8 -> Game(GameStarted(0, 10), Players(players(14), players(15))),
//        9 -> Game(GameNotReady, None),
//        10 -> Game(GameNotReady, None),
//        11 -> Game(GameNotReady, None),
//        12 -> Game(GameNotReady, None),
//        13 -> Game(GameNotReady, None),
//        14 -> Game(GameNotReady, None),
//        15 -> Game(GameNotReady, None)
//      )
//      val winnerFlow = List(
//        1 ->  9,  2 ->  9,
//        3 -> 10,  4 -> 10,
//        5 -> 11,  6 -> 11,
//        7 -> 12,  8 -> 12,
//        9 -> 13, 10 -> 13,
//        11 -> 14, 12 -> 14,
//        13 -> 15, 14 -> 15
//      )
//      SingleEliminationTournament(defaultDeadline, players, games, winnerFlow)
//    }
//  }

  case class DoubleEliminationTournament(normalDeadline: Int, players: List[String], games: Map[Int, Game], winnerFlow: List[(Int, Int)], loserFlow: List[(Int, Int)], currentTime: Int = 0) {

    def activeGames: List[(Int, Game)] = {
      for { (gameId, game @ Game(GameStarted(_, _), _, _)) <- games.toList }
      yield gameId -> game
    }

    def allFlows =
      for {
        (from, to) <- winnerFlow ++ loserFlow
        (sibling, `to`) <- winnerFlow ++ loserFlow
        if from != sibling
      } yield (to, games(to), games(from), games(sibling))

    def allFlowsId =
      for {
        (from, to) <- winnerFlow ++ loserFlow
        (sibling, `to`) <- winnerFlow ++ loserFlow
        if from != sibling
      } yield (to, games(to), from, games(from), sibling, games(sibling))

    def winningGameDependsOnWinnerWinner: List[(Int, Game, Int, Game, Int, Game)] = {
      for {
        (gameId, game @ Game(_, _, false)) <- games.toList
        (firstGameId, `gameId`) <- winnerFlow
        (secondGameId, `gameId`) <- winnerFlow
        if firstGameId < secondGameId
      } yield (gameId, game, firstGameId, games(firstGameId), secondGameId, games(secondGameId))
    }

    def losingGameDependsOnLoserLoser: List[(Int, Game, Int, Game, Int, Game)] = {
      for {
        (gameId, game @ Game(_, _, true)) <- games.toList
        (loserGameId1, `gameId`) <- loserFlow
        (loserGameId2, `gameId`) <- loserFlow
        if loserGameId1 < loserGameId2
      } yield (gameId, game, loserGameId1, games(loserGameId1), loserGameId2, games(loserGameId2))
    }

    def losingGameDependsOnWinnerLoser: List[(Int, Game, Int, Game, Int, Game)] = {
      for {
        (gameId, game @ Game(_, _, true)) <- games.toList
        (winnerGameId, `gameId`) <- winnerFlow
        (loserGameId, `gameId`) <- loserFlow
      } yield (gameId, game, winnerGameId, games(winnerGameId), loserGameId, games(loserGameId))
    }

    private def iterateMissedDeadlines: DoubleEliminationTournament = {
      val updatedGames = for {
        (gameId, game @ Game(gameStarted @ GameStarted(start, deadline), _, _)) <- games
        if currentTime > deadline
      } yield gameId -> game.copy(GameFinished(gameStarted, currentTime, GameFailed(s"deadline of $deadline missed, now $currentTime")))
      copy(games = games ++ updatedGames)
    }

    private def iterateTwoFails: DoubleEliminationTournament = {
      // any bracket: both have failed a game => fail the game itself
      val updatedGames = for {
        (id, nextGame @ Game(GameNotReady, _, _), firstId, Game(GameFinished(_, _, GameFailed(first)), _, _), secondId, Game(GameFinished(_, _, GameFailed(second)), _, _)) <- allFlowsId
      } yield id -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameFailed(s"both dependent games failed: $firstId ($first) $secondId ($second)")))
      copy(games = games ++ updatedGames)
    }

    private def iterateLoserStart: DoubleEliminationTournament = {
      val updatedGamesA = for {
        (id, nextGame @ Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(first, _)), _, false), Game(GameFinished(_, _, GameWon(_, Some(second))), _, true)) <- allFlows
      } yield id -> nextGame.copy(GameStarted(currentTime, currentTime+normalDeadline), Players(first, second))
      val updatedGamesB = for {
        (id, nextGame @ Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(_, Some(first))), _, true), Game(GameFinished(_, _, GameWon(_, Some(second))), _, true)) <- allFlows
      } yield id -> nextGame.copy(GameStarted(currentTime, currentTime+normalDeadline), Players(first, second))
      copy(games = games ++ updatedGamesA ++ updatedGamesB)
    }

    private def iterateTwoWins: DoubleEliminationTournament = {
      // two winners from winner brackets => start a new game in winners bracket
      // or: one winner from winner bracket + one winner from loser bracket
      val updatedGames = for {
        (id, nextGame @ Game(GameNotReady, _, true), Game(GameFinished(_, _, GameWon(first, _)), _, true), Game(GameFinished(_, _, GameWon(second, _)), _, _)) <- allFlows
      } yield id -> nextGame.started(currentTime, currentTime + normalDeadline, first, second)
      copy(games = games ++ updatedGames)
    }

    private def iteratePending: DoubleEliminationTournament = {
      // winners bracket: only one has completed a game, the other is pending => make it pending
      val A = for {
        (id, nextGame @ Game(GameNotReady, _, true), Game(GameFinished(_, _, GameWon(first, _)), _, _), Game(GameNotReady | GameStarted(_, _), _, _)) <- allFlows
      } yield id -> nextGame.copy(GameNotReady, players = Option(Players(Option(first), None)))
      // losers bracket: only one has completed a game, the other is pending => make it pending
      val B = for {
        (id, nextGame@Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(_, Some(loser))), _, true), Game(GameNotReady | GameStarted(_, _), _, _)) <- allFlows
      } yield id -> nextGame.copy(GameNotReady, players = Option(Players(Option(loser), None)))
      val C = for {
        (id, nextGame@Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(winner, None)), _, false), Game(GameNotReady | GameStarted(_, _), _, _)) <- allFlows
      } yield id -> nextGame.copy(GameNotReady, players = Option(Players(Option(winner), None)))
      copy(games = games ++ A ++ B ++ C)
    }

    private def iterateAutoWin: DoubleEliminationTournament = {
      // winners bracket: only one has completed a game, the other failed => automatic win
      val A = for {
        (id, nextGame @ Game(GameNotReady, _, true), Game(GameFinished(_, _, GameWon(first, _)), _, _), Game(GameFinished(_, _, GameFailed(_)), _, _)) <- allFlows
      } yield id -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(first, None)))
      // losers bracket: loser bracket completed game + winner bracket failed => automatic win for loser bracket
      val B = for {
        (id, nextGame @ Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(first, _)), _, false), Game(GameFinished(_, _, GameFailed(_)), _, true)) <- allFlows
      } yield id -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(first, None)))
      // losers bracket: loser bracket failed game + winner bracket lost => automatic win for winner bracket loser
      val C = for {
        (id, nextGame @ Game(GameNotReady, _, false), Game(GameFinished(_, _, GameWon(_, Some(first))), _, true), Game(GameFinished(_, _, GameFailed(_)), _, false)) <- allFlows
      } yield id -> nextGame.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(first, None)))
      copy(games = games ++ A ++ B ++ C)
    }

    def synced: DoubleEliminationTournament = {
      def fails(start: DoubleEliminationTournament): DoubleEliminationTournament = {
        start.iterateTwoFails match {
          case `start` => start
          case other => fails(other)
        }
      }
      def wins(start: DoubleEliminationTournament): DoubleEliminationTournament = {
        start.iterateAutoWin.iterateTwoWins match {
          case `start` => start
          case other => wins(other)
        }
      }
      wins(fails(this.iterateMissedDeadlines)).iteratePending
    }

    def withTime(newTime: Int): DoubleEliminationTournament = {
      copy(currentTime = newTime).synced
    }

    def withFailedGame(gameId: Int, reason: String): DoubleEliminationTournament = {
      val updatedGames =
        for {
          (`gameId`, game) <- games
        } yield (gameId, game match {
          case Game(started: GameStarted, _, _) => game.copy(GameFinished(started, currentTime, GameFailed(reason)))
          case other => other.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameFailed(reason)))
        })
      copy(games = games ++ updatedGames).synced
    }

    def withWonGame(gameId: Int, winner: String, loser: String): DoubleEliminationTournament = {
      val updatedGames =
        for {
          (`gameId`, game) <- games
        } yield (gameId, game match {
          case Game(started: GameStarted, _, _) => game.copy(GameFinished(started, currentTime, GameWon(winner, Option(loser))))
          case other => other.copy(GameFinished(GameStarted(currentTime, currentTime), currentTime, GameWon(winner, Option(loser))))
        })
      copy(games = games ++ updatedGames).iterateLoserStart.synced
    }

  }

}

object DoubleApp extends App {
  val yayd = DoubleElimination.DoubleEliminationTournament.startFour(10, List("John", "Betsy", "Raphael", "BigD"))
  val yay = yayd.withWonGame(1, "John", "Betsy")
  yay.games.toList.sortBy(_._1) foreach println
}