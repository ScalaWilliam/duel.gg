package gg.duel.tourney.newer

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

object Basics extends App {

  trait Event
  case class StartGame(gameId: Int, startTime: Int, deadline: Int) extends Event
  case class TimeTick(time: Int) extends Event
  case class SetWinner(gameId: Int, atTime: Int, winner: String) extends Event
  case class FailGame(gameId: Int, atTime: Int, reason: String) extends Event
  object Event {
    def fromXml(xml: scala.xml.Elem): Option[Event] = Option(xml) collectFirst {
      case start if start.label == "start" => StartGame(gameId = (start \@ "game-id").toInt, startTime = (start \@"start-time").toInt, deadline = (start \@ "deadline").toInt)
      case tick if tick.label == "time-tick" => TimeTick(time = (tick \@"time").toInt)
      case sw if sw.label == "set-winner" => SetWinner(gameId = (sw \@"game-id").toInt, atTime = (sw\@"at-time").toInt, winner = sw \@ "winner")
      case fg if fg.label == "fail-game" => FailGame(gameId = (fg \@"game-id").toInt, atTime = (fg\@"at-time").toInt, reason = fg \@"reason")
    }
  }

  class Slot(val id: Int) {
    var valueOverride = Option.empty[String]
    var winnerFromGame = Option.empty[Game]
    def value_=(name: String): Unit = { valueOverride = Option(name) }
    def value: Option[String] = valueOverride orElse winnerFromGame.flatMap(_.winner)
    def slotFailed = winnerFromGame.exists(_.gameFailed) : Boolean
    override def toString = s"""Slot($id, $value, Game(${winnerFromGame.map(_.id)}), failed = $slotFailed)"""
  }
  object Slot {
    def filled(id: Int)(name: String) = new Slot(id) { value = name }
    def winnerOf(id: Int)(game: Game) = new Slot(id) { winnerFromGame = Option(game) }
  }
  class Game(val id: Int, var firstSlot: Slot, var secondSlot: Slot) {
    var winnerOverride = Option.empty[String]
    var loserOverride = Option.empty[String]
    var failure = Option.empty[String]
    var gameFailed = false
    var startTimeO = Option.empty[Int]
    var deadlineO = Option.empty[Int]
    var finishedTimeO = Option.empty[Int]
    var canStartOverride = Option.empty[Boolean]
    def fail() = {
      gameFailed = true
    }
    def fail(reason: String) = {
      failure = Option(reason)
      gameFailed = true
    }
    def winner = {
      winnerOverride orElse {
        if ( firstSlot.slotFailed ) secondSlot.value
        else if ( secondSlot.slotFailed ) firstSlot.value
        else None
      }
    }
    def slots = List(firstSlot, secondSlot)
    def loser: Option[String] = {
      if ( winner.isEmpty ) None else {
        loserOverride orElse {
          firstSlot.value.toSet ++ secondSlot.value.toSet -- winner.toSet
        }.headOption
      }
    }
    def canStart_=(v: Boolean) = { canStartOverride = Option(v) }
    def canStart = {
      canStartOverride getOrElse {
        winner.isEmpty &&
          firstSlot.value.nonEmpty &&
          secondSlot.value.nonEmpty &&
          !gameFailed &&
          startTimeO.isEmpty
      }
    }
    def start(start: Int, deadline: Int): Unit = {
      if ( canStart ) {
        startTimeO = Option(start)
        deadlineO = Option(deadline)
      }
    }
    def winner_=(name: String) = {
      winnerOverride = Option(name)
    }
    def loser_=(name: String) = {
      loserOverride = Option(name)
    }
    def tick(time: Int): Unit =
      for { t <- deadlineO; if t < time } {
        fail(s"Game failed as it missed deadline of $t. Time now $time")
      }

    def win(name: String, atTime: Int): Unit = {
      winnerOverride = Option(name)
      loserOverride = loser
      finishedTimeO = Option(atTime)
    }

    override def toString = s"""Game($id, winner = $winner, loser = $loser, firstSlot = ${firstSlot.id}:${firstSlot.value}:${firstSlot.winnerFromGame.map(_.id)}, secondSlot = ${secondSlot.id}:${secondSlot.value}:${secondSlot.winnerFromGame.map(_.id)}, startTime = $startTimeO, deadlineO = $deadlineO, gameFailed = $gameFailed, failureReason = $failure)"""
  }

  object Game {
    def apply(id: Int, firstSlot: Slot, secondSlot: Slot) = new Game(id, firstSlot, secondSlot)
  }

  case class Tournament(players: List[String], games: List[Game], slots: List[Slot], events: collection.mutable.Buffer[Event]) {
    def toXml = <tournament size={players.size.toString}>
      {players.map(p => <player>{p}</player>)}
      {
      for { game <- games }
        yield <game id={game.id.toString} winner={game.winner.orNull} loser={game.loser.orNull}
        failed={game.gameFailed.toString} failure-reason={game.failure.orNull} start-time={game.startTimeO.map(_.toString).orNull}
      deadline={game.deadlineO.map(_.toString).orNull}
      finished-time={game.finishedTimeO.map(_.toString).orNull}
      can-start={game.canStart.toString}>
        { for { slot <- game.slots } yield <slot id={slot.id.toString} value={slot.value.orNull} from-game={slot.winnerFromGame.map(_.id.toString).orNull}/> }
      </game>
      }<events>
      {
      for { event <- events }
        yield event match {
          case StartGame(gameId, startTime, deadline) => <start game-id={gameId.toString} start-time={startTime.toString} deadline={deadline.toString}/>
          case TimeTick(time) => <time-tick time={time.toString}/>
          case SetWinner(gameId, atTime, winner) => <set-winner game-id={gameId.toString} winner={winner} at-time={atTime.toString}/>
          case FailGame(gameId, atTime, reason) => <fail-game game-id={gameId.toString} at-time={atTime.toString} reason={reason}/>
        }
      }</events>
    </tournament>
    def game(id: Int) = games.find(_.id == id).head
    def event(event: Event): Unit = {
      events += event
      event match {
        case StartGame(gameId, startTime, deadline) => game(gameId).start(startTime, deadline)
        case TimeTick(time) => games.foreach(_.tick(time))
        case SetWinner(gameId, atTime, winner) => game(gameId).win(winner, atTime)
        case FailGame(gameId, atTime, reason) => game(gameId).fail(reason)
      }
    }
  }
  object Tournament {
    def fromXml(input: scala.xml.Elem): Tournament = {
      val players = (input \ "player").map(_.text).toList
      val gamesMut = scala.collection.mutable.Map.empty[Int, Game]
      val games = for {
        gameXml <- (input \ "game").toIterator
        gameId = (gameXml \@ "id").toInt
        List(firstSlot, secondSlot) = for {
          slotXml <- (gameXml \ "slot").toList
          fromGame = (slotXml \ "@from-game").map(_.text.toInt).headOption
          slotId = (slotXml \@ "id").toInt
          slot = fromGame match {
            case Some(fromGameId) =>
              Slot.winnerOf(slotId)(gamesMut(fromGameId))
            case other =>
              Slot.filled(slotId)(slotXml \@ "value")
          }
        } yield {
          (slotXml \ "@value").foreach{s => slot.value = s.text}
          slot
        }
        game = new Game(gameId, firstSlot, secondSlot)
      } yield {
        gamesMut += gameId -> game
        gameXml \ "@winner" foreach (winner => game.winner = winner.text)
        gameXml \ "@loser" foreach (loser => game.loser = loser.text)
        game.gameFailed = (gameXml \@ "failed").toBoolean
        gameXml \ "@failure-reason" foreach (fr => game.failure = Option(fr.text))
        gameXml \ "@start-time" foreach(st => game.startTimeO = Option(st.text.toInt))
        gameXml \ "@deadline" foreach(d => game.deadlineO = Option(d.text.toInt))
        gameXml \ "@finished-time" foreach(ft => game.finishedTimeO = Option(ft.text.toInt))
        game.canStart = (gameXml \@ "can-start").toBoolean
        game
      }
      val gamesL = games.toList
      val slots = gamesL.flatMap(_.slots).toList
      val events = (input \ "events").flatMap(_.child).collect{ case e: scala.xml.Elem => Event.fromXml(e) }.flatten
      Tournament(players, gamesL, slots, collection.mutable.Buffer(events :_*))
    }
  }

  def powerfulTournament(players: List[String]): Tournament = {

    val number = Math.log(players.size)/Math.log(players.size)

    if ( number.floor != number ) {
      throw new IllegalArgumentException(s"Expected 2^n input, got $number")
    }

    val slotCounter = new AtomicInteger(0)
    val gameCounter = new AtomicInteger(0)

    def createNewGames(fromGames: List[Game]) = {
      val newGames = for {
        List(first, second) <- fromGames.sliding(2, 2).toList
        gameId = gameCounter.incrementAndGet()
        firstSlot = Slot.winnerOf(slotCounter.incrementAndGet())(first)
        secondSlot = Slot.winnerOf(slotCounter.incrementAndGet())(second)
      } yield new Game(gameId, firstSlot, secondSlot)
      newGames.toList
    }

    val initialGames = for {
      List(first, second) <- players.sliding(2,2).toList
    } yield new Game(gameCounter.incrementAndGet(), Slot.filled(slotCounter.incrementAndGet())(first), Slot.filled(slotCounter.incrementAndGet())(second))

    val games = Iterator.iterate(initialGames)(createNewGames).takeWhile(_.nonEmpty).flatten.toList
    val slots = games.flatMap(_.slots).toList
    Tournament(players, games, slots, mutable.Buffer.empty)

  }

  val t = powerfulTournament((1 to 16).map(s => s"U$s").toList)
  val games = t.games
  val slots = games.flatMap(g => g.slots).toList
  games.filter(_.id == 1).foreach(_.win("U1", 2))
  games.filter(_.id == 2).foreach(_.fail("Blah"))
  games.filter(_.id == 3).foreach(_.start(10, 12))
  games foreach (_.tick(13))
  games.filter(_.id == 4).foreach(_.win("U7", 2))
//  games foreach println

  val inputXml = t.toXml
  println(inputXml)
  val newT = Tournament.fromXml(inputXml)
  val nextXml = newT.toXml
  println(nextXml)
  println(inputXml == nextXml)



  val t2 = powerfulTournament((1 to 16).map(s => s"U$s").toList)
  t2.event(SetWinner(1, 2, "U1"))
  t2.event(FailGame(2, 2, "Blah"))
  t2.event(StartGame(3, 10, 12))
  t2.event(TimeTick(13))
  t2.event(SetWinner(4, 2, "U7"))

  val t2Xml = t2.toXml
  val t2r = Tournament.fromXml(t2Xml)
  val t2Xxml = t2r.toXml
  println(t2Xml == t2Xxml)
  println(t2Xml)
  println(t2Xxml)

}