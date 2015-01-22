package gg.duel.tourney.tournament


// slot = perfect, so stfu please.
class Slot(val id: Int) {
  var valueOverride = Option.empty[String]
  var winnerFromGame = Option.empty[Game]
  var loserFromGame = Option.empty[Game]
  def value_=(name: String): Unit = { valueOverride = Option(name) }
  def value = valueOverride orElse winnerFromGame.flatMap(_.winner) orElse loserFromGame.flatMap(_.loser)
  def dependsOn = winnerFromGame.toList ++ loserFromGame.toList
  def slotFailed: Boolean = value.isEmpty && (dependsOn.forall(_.gameFailed) || winnerFromGame.exists(g => g.isFinished && g.winner.isEmpty) || loserFromGame.exists(g => g.isFinished && g.loser.isEmpty))
}
object Slot {
  def filled(name: String)(implicit idGenerator: () => Int) = new Slot(idGenerator()) { value = name }
  def winnerOf(game: Game)(implicit idGenerator: () => Int) = new Slot(idGenerator()) { winnerFromGame = Option(game) }
  def loserOf(game: Game)(implicit idGenerator: () => Int) = new Slot(idGenerator()) { loserFromGame = Option(game) }
}