package us.woop.pinger.client
import akka.actor.ActorDSL._
import akka.actor.{ActorRef, ActorLogging}
import us.woop.pinger.client.data.{PingPongProcessor, IndividualServerProcessor, GlobalPingerClient}
import PingPongProcessor._
import us.woop.pinger.client.data.{IndividualServerProcessor, GlobalPingerClient}
import GlobalPingerClient._
import IndividualServerProcessor.ServerState
import PingPongProcessor.Ping
import PingPongProcessor.ReceivedMessage
import PingPongProcessor.BadHash
import GlobalPingerClient.Monitor
import GlobalPingerClient.Unmonitor
import akka.actor.Terminated



class GlobalPingerClient extends Act with ActorLogging with ActWithStash {

  val pinger = actor(context, name = "pinger")(new PingPongProcessor)

  val servers = scala.collection.mutable.HashMap[Server, ActorRef]()

  val listeners = scala.collection.mutable.Set[ActorRef]()

  def serverActor(forServer: Server) =
    actor(context, name = s"${forServer.ip.ip}:${forServer.port}")(new IndividualServerProcessor(forServer))

  become {
    case Ready(_) =>
      unstashAll()
      become {

        case m@ReceivedMessage(server, _, _) if servers contains server =>
          for {listener <- listeners} listener ! m
          for {child <- servers get server} child ! m

        case IndividualServerProcessor.Ping =>
          val child = sender()
          for {(server, `child`) <- servers}
            pinger ! Ping(server)

        case m@BadHash(server, _, _, _, _) if servers contains server =>
          for {child <- servers get server} child ! m

        case m: ServerState =>
          val child = sender()
          for {(server, `child`) <- servers; listener <- listeners} listener ! m

        case Monitor(server) if !(servers contains server) =>
          val newChild = serverActor(forServer = server)
          servers += server -> newChild
          context watch newChild

        case Unmonitor(server) if servers contains server =>
          for {actor <- servers remove server} context stop actor

        case Terminated(child) if listeners contains child =>
          listeners remove child
          context unwatch child

        case Terminated(child) =>
          context unwatch child
          for {(server, `child`) <- servers}
            servers += server -> serverActor(forServer = server)

        case Listen if !(listeners contains sender()) =>
          listeners += sender()
          context watch sender()

        case Unlisten if listeners contains sender() =>
          listeners -= sender()
          context unwatch sender()

      }
    case other =>
      stash()
  }

}
