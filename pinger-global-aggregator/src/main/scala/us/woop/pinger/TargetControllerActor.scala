package us.woop.pinger

import concurrent.duration._
import akka.actor.ActorDSL._
import akka.actor.ActorLogging
import scala.concurrent.duration.FiniteDuration
import us.woop.pinger.PingerServiceData.{SauerbratenPong, Unsubscribe, Server, Subscribe}
import us.woop.pinger.SauerbratenServerData.{OlderClient, ServerInfoReply}
import us.woop.pinger.client.PingPongProcessor.BadHash
import MasterserverClientActor._
import akka.event.LoggingReceive
import us.woop.pinger.WoopMonitoring.MonitorMessage

class TargetControllerActor extends Act with ActorLogging with WoopMonitoring {

  import TargetControllerActor._
  import scala.concurrent.ExecutionContext.Implicits.global

  val defaultRate = 5.seconds
  val serverRecords = collection.mutable.HashMap[(String, Int), ServerRecord]()

  val rateChange: PartialFunction[Int, FiniteDuration] = {
    case n if n > 6 => 1.second
    case n if n > 2 => 5.seconds
    case 0 => 20.seconds
  }

  def rateTo(server: (String, Int), duration: FiniteDuration, initialDelay: FiniteDuration = 0.seconds) =
    for { serverDesc <- serverRecords get server } {
      context.system.scheduler.scheduleOnce(
        delay = initialDelay,
        receiver = context.parent,
        message = Subscribe(Server(server._1, server._2), duration)
      )
    }
  
  context.system.scheduler.schedule(5.seconds, 10.seconds, self, FlushMetrics)

  case object FlushMetrics
  
  def addServer(server: (String, Int), initialDelay: FiniteDuration = 0.seconds) = {
    val serverDesc = new ServerRecord(rate = defaultRate, clients = 0, respondedLast = System.currentTimeMillis(), badHashes = 0, totalMessages = 0)
    serverRecords += server -> serverDesc
    rateTo(server, serverDesc.rate, initialDelay)
    log.info("Adding server {}", server)
    serverDesc
  }

  become (LoggingReceive {
    case MasterServers(servers) =>
      for {
        (server, index) <- servers.diff(serverRecords.keySet).zipWithIndex
        launchIn = index * 30.seconds / servers.size
      } addServer(server, launchIn)
      log.info("Target controller initialised with {} servers", servers.size)
      context.system.scheduler.schedule(0.seconds, 5.minutes, self, CheckGoners)
      context.system.scheduler.schedule(5.seconds, 1.minute, self, ResetBadHashCounts)
      become(initialised)
  })

  def initialised: Receive = {

    case FlushMetrics =>
      notify(MonitorMessage('totalMessages, serverRecords.values.map{_.totalMessages}.sum))
      notify(MonitorMessage('serverRecords,
      {
        val r = for { (server, desc) <- serverRecords }
        yield Map(
          'server -> server,
          'badHashes -> desc.badHashes,
          'clients -> desc.clients,
          'rate -> desc.rate,
          'respondedLast -> desc.respondedLast,
          'totalMessages -> desc.totalMessages
        )
        r.toList
      }))

      for { desc <- serverRecords.values} desc.totalMessages = 0

    case ServerAdded(server) if !(serverRecords contains server) =>
      addServer(server)

    case ServerGone(server) if serverRecords contains server =>
      val serverDesc = serverRecords(server)
      if (serverDesc.clients == 0) {
        rateTo(server, 5.minutes)
        log.info("Server {} made as if removed", server)
      } else {
        log.info("Server gone, but not removed as it has clients", server)
      }

    case SauerbratenPong(_, server, reply: ServerInfoReply) if serverRecords contains server =>
      val serverDesc = serverRecords(server)
      serverDesc.respondedLast = System.currentTimeMillis
      serverDesc.clients = reply.clients
      val oldRate = serverDesc.rate
      for {
        newRate <- rateChange.lift.apply(serverDesc.clients)
        if newRate != oldRate
      } rateTo(server, newRate)
      serverDesc.totalMessages = serverDesc.totalMessages + 1

    case ResetBadHashCounts =>
      log.debug("Resetting bad hash counts")
      for { (_, serverDesc) <- serverRecords }
        serverDesc.badHashes = 0

    case SauerbratenPong(_, server, reply: BadHash) if serverRecords contains server =>
      for { serverDesc <- serverRecords get server }
      serverDesc.badHashes = serverDesc.badHashes + 1

    case SauerbratenPong(_, server, _: OlderClient) if serverRecords contains server =>
      serverRecords remove server
      log.info("Server {} is old, removing", server)
      context.parent ! Unsubscribe(Server(server._1, server._2))

    case CheckGoners =>
      for {
        (server, serverDesc) <- serverRecords
        if (System.currentTimeMillis - serverDesc.respondedLast).toInt.millis.toMinutes >= 2
      } rateTo(server, 2.minutes)
  }

}

object TargetControllerActor {

  class ServerRecord(var rate: FiniteDuration, var clients: Int, var respondedLast: BigInt, var badHashes: Int, var totalMessages: Int)

  case object CheckGoners

  case object ResetBadHashCounts

}