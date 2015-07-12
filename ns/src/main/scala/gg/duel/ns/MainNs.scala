package gg.duel.ns

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import gg.duel.pinger.analytics.MultiplexedReader.{SFoundGame, CompletedGame, SIteratorState, SInitial}
import gg.duel.pinger.data.{ServersListing, Server}
import gg.duel.pinger.data.journal.SauerBytes
import gg.duel.pinger.service.PingPongProcessor.{ReceivedBytes, Ready, ReceiveResult, Ping}
import gg.duel.pinger.service.{PingPongProcessorState, PingPongProcessorActor}
import concurrent.duration._
import akka.actor.ActorDSL._

case class PingerState
(
  servers: Set[Server],
  sIteratorState: SIteratorState
)
object PingerState {
  def empty = PingerState(
    servers = Set.empty,
    sIteratorState = SIteratorState.empty
  )
}


object MainNs extends App {

  val processor: SIteratorState = SInitial

  val servers = ServersListing.servers

  implicit val actorSystem = ActorSystem("Moneyyy")

  implicit val mat = ActorMaterializer()
  import actorSystem.dispatcher
  
  val pingerStateAgt = Agent(PingerState.empty)

  pingerStateAgt.send(_.copy(servers = servers.toSet))

  val conactor = actor(name = "wut")(new Act {

    val pingPong = context.actorOf(
      name = "ping-pong",
      props = PingPongProcessorActor.props(initialState = PingPongProcessorState.empty)
    )

    val strm = Source.actorRef[ReceiveResult](bufferSize = 100, overflowStrategy = OverflowStrategy.fail)

    val flw = Flow[ReceiveResult]
      .collect{
      case r: ReceivedBytes =>
        r.toSauerBytes
    }.scan(SIteratorState.empty)(_.next(_))
      .map{ sst =>
        pingerStateAgt.send(_.copy(sIteratorState = sst))
        sst
    }
    .to(Sink.foreach(println)).runWith(strm)

    become {
      case Ready(_) =>
        Source.apply(initialDelay = 1.second, interval = 5.seconds, tick = "HEL")
          .to(Sink.actorRef(self, 0))
          .run()
        become {
          case "HEL" =>
            pingerStateAgt.get().servers.foreach(pingPong ! Ping(_))
          case r: ReceiveResult =>
            println("Got something", r)
            flw ! r
        }
    }

  })

  actorSystem.scheduler.scheduleOnce(5.seconds)(actorSystem.shutdown())

}