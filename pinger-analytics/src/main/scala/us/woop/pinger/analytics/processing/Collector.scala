package us.woop.pinger.analytics.processing

import akka.stream.Transformer
import akka.stream.scaladsl.Flow
import org.joda.time.format.ISODateTimeFormat
import org.reactivestreams.api.Producer
import us.woop.pinger.PingPongProcessor
import us.woop.pinger.analytics.data.{GameData, ModesList}
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.ConvertedServerInfoReply
import us.woop.pinger.data.ParsedPongs.ParsedMessage
import us.woop.pinger.data.ParsedPongs.TypedMessages.ParsedTypedMessages.ParsedTypedMessageConvertedServerInfoReply

import scala.annotation.tailrec
import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.xml.Elem

object Collector {

  // This is a finite state machine. Very similar to scalaz-stream but much pretrier
  sealed trait State
  sealed trait Open extends State {
    def process: PartialFunction[ParsedMessage, Open]
    def close: Closed
  }

  sealed trait Closed extends State
  case object ClosedWithNoData extends Closed
  case class ClosedWithData(data: GameData*) extends Closed

  case class EmmittedOpen(emmittee: GameData, nextData: GameData) extends Open {
    def process = InProgressOpen(nextData).process
    def close = ClosedWithData(emmittee, nextData)
  }

  case class InProgressOpen(data: GameData) extends Open {
    def process = {
      case statusMessage @ ParsedTypedMessageConvertedServerInfoReply(convertedMessage)
        if isSwitch(data.firstTime.message, convertedMessage.message) =>
      EmmittedOpen(
        emmittee = data.copy(nextGame = Option(convertedMessage)),
        nextData = GameData(
          firstTime = convertedMessage,
          nextGame = None,
          gameMessages = List(statusMessage)
        )
      )
      case other =>
        InProgressOpen(data.copy(gameMessages = data.gameMessages :+ other))
    }
    def close = ClosedWithData(data)
  }

  case object NoGameOpen extends Open {
    def process = {
      case statusMessage @ ParsedTypedMessageConvertedServerInfoReply(convertedMessage) =>
        InProgressOpen(
          data = GameData(
            firstTime = convertedMessage ,
            nextGame = None,
            gameMessages =  List(statusMessage)
          )
        )
      case other => NoGameOpen
    }
    def close = ClosedWithNoData
  }

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  def multiplexFlows(proc: Producer[ParsedMessage]): Flow[GameData] =
    Flow(proc).transform(new Transformer[ParsedMessage, GameData]{
      var stateMap = Map.empty[PingPongProcessor.Server, Open].withDefaultValue(NoGameOpen)
      override def onNext(gameData: ParsedMessage): Seq[GameData] = {
        val nextState = stateMap(gameData.server).process.apply(gameData)
        stateMap = stateMap.updated(gameData.server, nextState)
        immutable.Seq(nextState).collect {
          case EmmittedOpen(emmittee, _) => emmittee
        }
      }
    })

  // synchronous data extractor, does a similar job to the above
  def extractData(from: Iterator[ParsedMessage]) = {
    @tailrec
    def gameDataExtractor(currentState: State, accum: Stream[GameData], toDo: Stream[ParsedMessage]): Stream[GameData] = {
      (currentState, toDo) match {
        case (ClosedWithNoData, _) => accum
        case (ClosedWithData(data@_*), Stream.Empty) =>
          gameDataExtractor(ClosedWithNoData, accum #::: data.toStream, Stream.empty)
        case (EmmittedOpen(first, second), Stream.Empty) =>
          accum append Stream(first) append Stream(second)
        case (s@EmmittedOpen(first, _), h #:: re) =>
          gameDataExtractor(s.process(h), accum append Stream(first), re)
        case (s: Open, h #:: re) =>
          gameDataExtractor(s.process(h), accum, re)
        case (s: Open, Stream.Empty) =>
          gameDataExtractor(s.close, accum, toDo)
      }
    }

    gameDataExtractor(NoGameOpen, Stream.empty, from.toStream)
  }


  def processGameData(x: GameData): Either[Elem, Elem] = {
    val duel = DuelMaker.makeDuel(x)
    val cm = ClanmatchMaker.makeMatch(x)
    List(duel, cm).collectFirst{
      case Right(goodGame) => Right(goodGame)
    }.getOrElse {
      Left(
      <othergame>
        <server>{x.firstTime.server.ip.ip}:{x.firstTime.server.port}</server>
        <map>{x.firstTime.message.mapname}</map>
        <timestamp>{ISODateTimeFormat.dateTimeNoMillis().print(x.firstTime.time)}</timestamp>
        {x.firstTime.message.gamemode.flatMap(ModesList.modes.get).toSeq.map{mode => <mode>{mode.name}</mode>}}
        <failures>
          <duel>{duel.left.get}</duel>
          <clanmatch>{cm.left.get}</clanmatch>
        </failures>
      </othergame>)
    }
  }


  // only used for testing

  trait GetGameImperative {
    var gameProcessorState: Either[Closed, Open] = Right(NoGameOpen)
    def emit(data: GameData)
    def input(message: ParsedMessage) {
      gameProcessorState = Right(gameProcessorState.right.get.process.apply(message))
      gameProcessorState.right.get match {
        case EmmittedOpen(game, _) => emit(game)
        case _ =>
      }
    }
    def complete() {
      gameProcessorState = Left(gameProcessorState.right.get match {
        case EmmittedOpen(first, second) =>
          ClosedWithData(first, second)
        case InProgressOpen(game) =>
          ClosedWithData(game)
        case NoGameOpen =>
          ClosedWithNoData
      })
      gameProcessorState.left.get match {
        case ClosedWithData(games @ _*) =>
          games foreach emit
        case _ =>
      }
    }
  }
}
