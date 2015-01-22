package gg.duel.tourney.tournament


class Game(val id: Int, var firstSlot: Slot, var secondSlot: Slot) {
  var winnerOverride = Option.empty[String]
  var loserOverride = Option.empty[String]
  var failure = Option.empty[Option[String]]
  def slots = List(firstSlot, secondSlot)
  def gameFailed = failure.nonEmpty || (winner.isEmpty && slots.forall(_.slotFailed))
  def winner: Option[String] = winnerOverride orElse {
    if ( firstSlot.slotFailed ) secondSlot.value
    else if ( secondSlot.slotFailed ) firstSlot.value
    else None
  }
  def loser: Option[String] = loserOverride orElse {
    for {
      w <- winner
      l <- (firstSlot.value.toSet ++ secondSlot.value.toSet - w).headOption
    } yield l
  }
  var isFinishedOverride = Option.empty[Boolean]
  def isFinished = isFinishedOverride.getOrElse(gameFailed || winner.isDefined)
  def isFinished_=(value: Boolean) = {isFinishedOverride = Option(value)}
  def winner_=(name: String): Unit = { winnerOverride = Option(name) }
  def loser_=(name: String): Unit = { loserOverride = Option(name) }
  def fail(): Unit = { failure = Option(None) }
  def fail(reason: String): Unit = { failure = Option(Option(reason)) }
  var started: Boolean = false
  def start(): Unit = { started = true }
  def isRunning = winner.isEmpty && !gameFailed && started
  var canStartOverride = Option.empty[Boolean]
  def canStart = canStartOverride.getOrElse(winner.isEmpty && !gameFailed && firstSlot.value.nonEmpty && secondSlot.value.nonEmpty && !started)
  def canStart_=(value: Boolean): Unit = { canStartOverride = Option(value) }
  override def toString = s"""Game($id, firstSlot = Slot(${firstSlot.id}, value = ${firstSlot.value}, failed = ${firstSlot.slotFailed}, l=${firstSlot.loserFromGame.map(_.id)}, w = ${firstSlot.winnerFromGame.map(_.id)}), secondSlot = Slot(${secondSlot.id}, value = ${secondSlot.value}, failed = ${secondSlot.slotFailed}, l=${secondSlot.loserFromGame.map(_.id)}, w = ${secondSlot.winnerFromGame.map(_.id)}), finished = $isFinished, winner = $winner, loser = $loser, gameFailed = $gameFailed, isRunning = $isRunning, canStart = $canStart, failure = $failure)"""
}
object Game {
  def apply(id: Int, firstSlot: Slot, secondSlot: Slot) = new Game(id, firstSlot, secondSlot)
}