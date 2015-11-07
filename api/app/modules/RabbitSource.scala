package modules

/**
  * Created by William on 07/11/2015.
  */
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import gg.duel.SimpleGame
import io.scalac.amqp.Connection
import io.scalac.amqp.Delivery
import play.api.libs.EventSource.Event
import play.api.{Logger, Configuration}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}

trait RabbitSource {

}

class NoRabbitSource @Inject()() extends RabbitSource {
  Logger.info("Rabbit games not active")
}

@Singleton
class RabbitSourceRunning @Inject()(gamesService: GamesService,
                                    configuration: Configuration)(
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

  import gamesService.sgToEvent
  val toNewGamesSink = Sink.foreach[SimpleGame] { sg =>
    gamesService.newGamesChan.push(sg -> sg.toEvent)
  }

  val toLiveGamesSink = Sink.foreach[(SimpleGame, Event)] {
    sg => gamesService.liveGamesChan.push(sg)
  }

  val toAgentSink = Sink.foreach[SimpleGame] { sg =>
    gamesService.gamesAgt.send(_ + sg)
  }

  val qs = Source(queue)

  val fg = FlowGraph.closed(qs, toNewGamesSink, toLiveGamesSink, toAgentSink)((_, _, _, _)) { implicit builder =>
    (qsrc, ngs, lgs, as) =>
      import FlowGraph.Implicits._
      val deliveryToGame = Flow[Delivery].mapConcat { delivery =>
        gamesService.sseToGameOption.apply(
          id = delivery.message.headers("game-id"),
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
  val (_, a, b, c) = fg.run()

  def completeF(f: Future[_]): Unit = {
    f.onComplete {
      case Success(_) => Logger.info("Ok completed rabbit flow")
      case Failure(r) => Logger.error("Rabbit flow failed due to an error", r)
    }
  }
  completeF(a)
  completeF(b)
  completeF(c)
}