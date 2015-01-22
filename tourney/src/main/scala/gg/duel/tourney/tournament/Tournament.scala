package gg.duel.tourney.tournament

import java.util.concurrent.atomic.AtomicInteger


trait Tournament {
  class Slots {
    val slotSet = collection.mutable.Set.empty[Slot]
    def apply(num: Int): Slot = slotSet.filter(_.id == num).head
    def +=(slot: Slot): Unit = { slotSet += slot; }
  }
  class Games(slots: Slots) {
    val gameSet = collection.mutable.Set.empty[Game]
    def apply(num: Int): Game = gameSet.filter(_.id == num).head
    def +=(game: Game): Unit = { gameSet += game; slots += game.firstSlot; slots += game.secondSlot }
    def ++=(games: TraversableOnce[Game]): Unit = {gameSet ++= games}
    def start(): Unit = { gameSet.filter(_.canStart).foreach(_.start()) }
    def debug(): Unit = { gameSet.toList.sortBy(_.id).foreach(println) }
    def getStartableGames = gameSet.filter(_.canStart)
  }
  val slots = new Slots
  def tournamentType: String
  val games = new Games(slots)
  def finalGame: Game
  def players: List[String]
  val currentId = new AtomicInteger(1)
  implicit val slotIdGenerator = () => currentId.getAndIncrement


  def toXml = {
    val tournament = this
    <tournament type={tournament.tournamentType} final-game={tournament.finalGame.id.toString}>
      {for { player <- tournament.players }
    yield <player>{player}</player>
      }

      {
      for { game <- tournament.games.gameSet.toList.sortBy(_.id) }
      yield <game id={game.id.toString}
                  winner={game.winner.orNull}
                  loser={game.loser.orNull}
                  failed={game.gameFailed.toString}
                  fail-reason={game.failure.flatten.orNull}
                  can-start={game.canStart.toString}
                  started={game.started.toString}
                  is-finished={game.isFinished.toString}>
        {for {slot <- game.slots}
        yield slot.value match {
            case Some(value) => <slot id={slot.id.toString}
                                      winner-from={slot.winnerFromGame.map(_.id.toString).orNull}
                                      loser-from={slot.loserFromGame.map(_.id.toString).orNull}>{value}</slot>
            case None => <slot id={slot.id.toString}
                               winner-from={slot.winnerFromGame.map(_.id.toString).orNull}
                               loser-from={slot.loserFromGame.map(_.id.toString).orNull}/>
          }}
      </game>
      }
    </tournament>
  }
}

object Tournament {

  def loadFromXml(input: scala.xml.Elem): Tournament = {
    new Tournament {
      override val players = (input \ "player").map(_.text).toList
      override val tournamentType = input \@ "type"
      for {
        xgame <- input \ "game"
        id = (xgame \@ "id").toInt
      } {
        val List(firstSlot, secondSlot) = for {
          xslot <- (xgame \ "slot").toList
          slotId = (xslot \@ "id").toInt
          slotValue = Option(xslot.text).filter(_.nonEmpty)
          slotWinnerGame = (xslot \ "@winner-from").map(_.text.toInt).headOption
          slotLoserGame = (xslot \ "@loser-from").map(_.text.toInt).headOption
        } yield {
          val slot =
            if (slotWinnerGame.nonEmpty) Slot.winnerOf(games(slotWinnerGame.get))(() => slotId)
            else if (slotLoserGame.nonEmpty) Slot.loserOf(games(slotLoserGame.get))(() => slotId)
            else if (slotValue.nonEmpty) Slot.filled(slotValue.get)
            else throw new IllegalStateException(s"Slot invalid: $xslot")
          slotValue.foreach { v => slot.value = v}
          slot
        }
        val game = new Game(id, firstSlot, secondSlot)
        games += game
        xgame.attribute("winner").map(_.text).foreach(game.winner = _)
        xgame.attribute("loser").map(_.text).foreach(game.loser = _)
        game.failure = if ((xgame \@ "failed").toBoolean) {
          Option(xgame.attribute("fail-reason").map(_.text).headOption)
        } else {
          None
        }
        game.canStart = (xgame \@ "can-start").toBoolean
        game.started = (xgame \@ "started").toBoolean
        game.isFinished = (xgame \@ "is-finished").toBoolean
      }
      override val finalGame = games((input \@ "final-game").toInt)
    }
  }
}