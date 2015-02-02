package gg.duel.tourney.newer

import gg.duel.tourney.newer.Immutability.Tournament

object Immutability {
  sealed trait GameState
  case object GameAwaitingSlots extends GameState
  case class GameFailed(reason: String) extends GameState
  case class GameWon(winner: String) extends GameState
  case object GameAwaitingResult extends GameState

  sealed trait SlotState
  case object SlotAwaiting extends SlotState
  case class SlotFailed(reason: String) extends SlotState
  case class SlotFilled(name: String) extends SlotState

  sealed abstract class GameSlot(val gameId: Int, val slotId: Int)
  case class LeftSlot(override val gameId: Int, override val slotId: Int) extends GameSlot(gameId, slotId)
  case class RightSlot(override val gameId: Int, override val slotId: Int) extends GameSlot(gameId, slotId)
  object GameSlot {
    // boolean: isRight
    def unapply(gameSlot: GameSlot): Option[(Int, Int, Boolean)] = {
      Option(gameSlot).collect {
        case LeftSlot(a, b) => (a, b, false)
        case RightSlot(a, b) => (a, b, true)
      }
    }
  }

  case class TournamentWithTime(tournament: Tournament, deadlines: Map[Int, Int], expectedDuration: Int, completions: Map[Int, Int]) {
    def withGameTick(time: Int): TournamentWithTime = {
      val gamesToFail = for {
        (_, gameId) <- tournament.resultsWouldCompleteGames
        deadline <- deadlines get gameId
        if time == deadline
      }  yield gameId
      val newTournament = gamesToFail.foldLeft(tournament)(tournament.withGameFailed(_, s"Deadline $time reached"))
      val extraCompletions = gamesToFail.map(g => g -> time)
      val newGamesIds = tournament.resultsWouldCompleteGames.map(_._2).toSet.diff(deadlines.keySet)
      val extraDeadlines = newGamesIds.map(gId => gId -> (time + expectedDuration))
      val newDeadlines = deadlines ++ extraDeadlines
      val newCompletions = completions ++ extraCompletions.toMap
      // then look for newly open slots so we can give them
      copy(tournament = newTournament, completions = newCompletions, deadlines = newDeadlines)
    }
    def withSlotFailed(slotId: Int, reason: String): TournamentWithTime = {

    }
  }

  case class Tournament(slots: Map[Int, SlotState], games: Map[Int, GameState], gameSlots: Set[GameSlot], winnerFillsSlot: Map[Int, Int]) {
    def withSlotFailed(slotId: Int, reason: String): Tournament = {
      val upstreamGameIdO = winnerFillsSlot.find(_._2 == slotId).map(_._1).map(gId => gId -> games(gId))
      val upstreamGameFailure = upstreamGameIdO.collect{case (gId, GameAwaitingResult | GameAwaitingSlots) => gId -> GameFailed(s"Downstream slot $slotId failed with reason: $reason")}.toMap
      val newerTournament = copy(slots = slots.updated(slotId, SlotFailed(reason)), games = games ++ upstreamGameFailure)
      val currentGameId = gameSlots.find(_.slotId == slotId).map(_.gameId).get
      val siblingSlot = gameSlots.find(g => g.gameId == currentGameId && g.slotId != slotId).map(_.slotId).get
      slots(siblingSlot) match {
        case SlotFailed(secondReason) =>
          if ( gameSlots.filter(_.gameId == currentGameId).find(_.slotId == slotId).get.isInstanceOf[LeftSlot] ) {
            newerTournament.withGameFailed(currentGameId, s"Both slots $slotId and $siblingSlot failed: ($reason, $secondReason)")
          } else {
            newerTournament.withGameFailed(currentGameId, s"Both slots $siblingSlot and $slotId failed: ($secondReason, $reason)")
          }
        case SlotAwaiting =>
          newerTournament
        case SlotFilled(user) =>
          newerTournament.withGameWinner(currentGameId, user)
      }
    }
    def resultsWouldCompleteGames: Set[((String, String), Int)] = {
      for {
        (gameId, GameAwaitingResult) <- games.toSet
        LeftSlot(`gameId`, leftSlotId) <- gameSlots
        RightSlot(`gameId`, rightSlotId) <- gameSlots
        SlotFilled(leftName) <- slots.get(leftSlotId)
        SlotFilled(rightName) <- slots.get(rightSlotId)
      } yield ((leftName, rightName), gameId)
    }
    def withSlotFilled(slotId: Int, name: String): Tournament = {
      val newerTournament = copy(slots = slots.updated(slotId, SlotFilled(name)))
      val downstreamGameId = gameSlots.find(_.slotId == slotId).map(_.gameId).get
      val siblingSlot = gameSlots.find(g => g.gameId == downstreamGameId && g.slotId != slotId).map(_.slotId).get
      slots(siblingSlot) match {
        case SlotFailed(_) =>
          newerTournament.withGameWinner(downstreamGameId, name)
        case SlotAwaiting =>
          newerTournament.copy(games = games.updated(downstreamGameId, GameAwaitingSlots))
        case SlotFilled(other) =>
          newerTournament.copy(games = games.updated(downstreamGameId, GameAwaitingResult))
      }
    }
    def withGameWinner(gameId: Int, name: String): Tournament = {
      val newerTournament = copy(games = games.updated(gameId, GameWon(name)))
      winnerFillsSlot.get(gameId) match {
        case Some(targetSlot) =>
          newerTournament.withSlotFilled(targetSlot, name)
        case _ =>
          newerTournament
      }
    }
    def withGameFailed(gameId: Int, reason: String): Tournament = {
      val newerTournament = copy(games = games.updated(gameId, GameFailed(reason)))
      winnerFillsSlot.get(gameId) match {
        case Some(targetSlot) =>
          newerTournament.withSlotFailed(targetSlot, s"Upstream game $gameId failed: $reason")
        case None =>
          newerTournament
      }
    }
  }
  object Tournament {
    def fourPlayers(names: Vector[String]) = {
      val slots = names.zipWithIndex.map{case (name, num) => (num + 1) -> SlotFilled(name)}.toMap ++ Map(5 -> SlotAwaiting, 6 -> SlotAwaiting)
      val games = Map(1 -> GameAwaitingResult, 2-> GameAwaitingResult, 3-> GameAwaitingSlots)
      val gameSlots = Set(LeftSlot(1, 1), RightSlot(1, 2), LeftSlot(2, 3), RightSlot(2, 4), LeftSlot(3, 5), RightSlot(3, 6)).map(g => g:GameSlot)
      val winnerFillsSlot = Map(1 -> 5, 2 -> 6)
      Tournament(slots, games, gameSlots, winnerFillsSlot)
    }
  }

}
object ImmyTestApp extends App {
  val initial = Tournament.fourPlayers(Vector("A", "B", "C", "D"))
  println(initial)
  println("successes")
  println(initial.withGameWinner(1, "A"))
  println(initial.withGameWinner(1, "A").withGameWinner(2, "C"))
  println(initial.withGameWinner(1, "A").withGameWinner(2, "C").withGameWinner(3, "C"))
  println("--")
  println("failures 1")
  println(initial.withGameFailed(1, "Alpha"))
  println(initial.withGameFailed(1, "Beta").withGameFailed(2, "Beta"))
  println("failures 2")
  println(initial.withGameFailed(1, "Delta").withGameWinner(2, "C"))
  println(initial.withGameWinner(2, "C").withGameFailed(1, "Delta"))
  println("failures 3")
  println(initial.withGameFailed(2, "Gamma").withGameWinner(1, "B"))
  println(initial.withGameWinner(1, "B").withGameFailed(2, "Gamma"))
}