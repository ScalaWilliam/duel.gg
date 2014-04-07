package us.woop.contained

import akka.actor._
import us.woop.pinger.{PingerServiceData, MasterserverClientActor}
import us.woop.pinger.client.ServerPinger
import java.net.InetAddress
import us.woop.pinger.client.ServerPinger.{PingRate, Fast}
import us.woop.pinger.MasterserverClientActor.{ServerGone, ServerAdded, MasterServers, RefreshServerList}
import us.woop.pinger.MasterserverClientActor.ServerGone
import us.woop.pinger.MasterserverClientActor.MasterServers
import akka.actor.Terminated
import us.woop.pinger.MasterserverClientActor.ServerAdded
import us.woop.pinger.client.PingPongProcessor.ParsedMessage
import java.io.File

object Contained extends App {
  import concurrent.duration._
  import akka.actor.ActorDSL._

      val addra = (InetAddress.getByName("sauer.woop.us").getHostAddress, PingerServiceData.defaultSauerbratenPort)
  //    servers += addr -> context.actorOf(ServerPinger.buildStandard(addr), name = "purp")
  //    servers += addr -> context.actorOf(ServerPinger.buildStandardWithRate(addr, Fast), name = "purp")

  case object Firehose
  case object Unfirehose

  val as = ActorSystem("Derpen")

  val master = actor(as, "actorPlugger")(new Act {

    val masterserverClient = actor(context, "masterserverClient")(new MasterserverClientActor)

    val servers = collection.mutable.HashMap[(String, Int), ActorRef]()

    val levelDbPersister = actor(context, "levelDbPersister")(new Persister(new File("./test-data")))

    var refreshSchedule: Cancellable = _

    val firehose = collection.mutable.Set[ActorRef]()

    whenStarting {
      import scala.concurrent.ExecutionContext.Implicits.global
      refreshSchedule = context.system.scheduler.schedule(2.seconds, 5.minutes, masterserverClient, RefreshServerList)
    }

    whenStopping {
      refreshSchedule.cancel()
    }

    def add(addr: (String, Int)) = {
      val name = s"${addr._1}:${addr._2}"
      val newActor = context.actorOf(ServerPinger.buildStandard(addr), name = name)
      servers += addr -> newActor
      newActor
    }

    def addWithRate(addr: (String, Int), rate: PingRate) = {
      val name = s"${addr._1}:${addr._2}"
      val newActor = context.actorOf(ServerPinger.buildStandardWithRate(addr, rate), name = name)
      servers += addr -> newActor
      newActor
    }

    become {

      case Firehose =>
        firehose += sender()

      case Unfirehose =>
        firehose -= sender()

      case MasterServers(list) =>
        list filterNot { servers.contains } foreach { add }

      case ServerAdded(newServer) if !(servers contains newServer) =>
        add(newServer)

      case ServerGone(goneServer) if servers contains goneServer =>

      case Terminated(actor) =>
        context unwatch actor
        val addrs = for { (addr, `actor`) <- servers; _ <- servers remove addr } yield addr
        addrs foreach add
        firehose remove actor

      case m: ParsedMessage =>
        for { receiver <- firehose } receiver ! m

    }
  })




}
