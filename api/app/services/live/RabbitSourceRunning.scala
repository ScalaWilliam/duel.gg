package services.live

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import gg.duel.SimpleGame
import io.scalac.amqp.{Connection, Delivery}
import lib.StopperFlow
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.{Configuration, Logger}
import services.games.GamesService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class RabbitSourceRunning @Inject()(gamesService: GamesService,
                                    configuration: Configuration,
                                    applicationLifecycle: ApplicationLifecycle)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem
) extends RabbitSource{
  Logger.info("Loading rabbit games...!")
  implicit val am = ActorMaterializer()

  // streaming invoices to Accounting Department
  val connection = Connection(configuration.underlying)
  val queue = connection.consume(
    queue = configuration.getString("gg.duel.queue-name").getOrElse("Whoops, 'gg.duel.queue-name' missing.")
  )
  import GamesService.sgToEvent
  val toNewGamesSink = Sink.foreach[SimpleGame] { sg =>
    gamesService.newGamesChan.push(Option(sg) -> sg.toEvent)
  }

  val toLiveGamesSink = Sink.foreach[(SimpleGame, Event)] {
    case (sg, event) => gamesService.liveGamesChan.push(Option(sg) -> event)
  }

  val toAgentSink = Sink.foreach[SimpleGame] { sg =>
    gamesService.gamesAgt.send(_ + sg)
  }

  val qs = Source(queue).viaMat(StopperFlow())(Keep.both)

  val fg = FlowGraph.closed(qs, toNewGamesSink, toLiveGamesSink, toAgentSink)((_, _, _, _)) { implicit builder =>
    (qsrc, ngs, lgs, as) =>
      import FlowGraph.Implicits._
      val deliveryToGame = Flow[Delivery].mapConcat { delivery =>
        gamesService.sseToGameOption.apply(
          json = delivery.message.body.decodeString("UTF-8")
        ).map { sseGame => delivery -> sseGame }.toList

      }

      val collectNew = Flow[(Delivery, SimpleGame)].collect {
        case (del, sg) if Set("ctf", "duel") contains del.routingKey => sg
      }
      val processLiveGame = Flow[(Delivery, SimpleGame)].collect {
        case (del, sg) => sg -> sg.toEvent.copy(name = Option(del.routingKey))
      }


      val bcast = builder.add(Broadcast[(Delivery, SimpleGame)](3))
      qsrc ~> deliveryToGame ~> bcast
      bcast ~> collectNew ~> ngs
      bcast ~> processLiveGame ~> lgs
      bcast ~> collectNew ~> as

  }

  // not sure how to shut this one down :-O
  val ((_, stopPromise), a, b, c) = fg.run()

  def completeF(f: Future[_]): Unit = {
    f.onComplete {
      case Success(_) => Logger.info("Ok completed rabbit flow")
      case Failure(r) => Logger.error("Rabbit flow failed due to an error", r)
    }
  }
  completeF(a)
  completeF(b)
  completeF(c)

  applicationLifecycle.addStopHook(() =>

  Future.successful(stopPromise.success(())))

}
