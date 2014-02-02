package us.woop

import akka.actor.{Props, Actor}
import us.woop.SauerbratenPinger._

import akka.actor.ActorDSL._
import us.woop.SauerbratenPinger.InetPair
import us.woop.SauerbratenPinger.Ping

/** 02/02/14 */
class PingPongProcessor extends Actor {
  val child = context.actorOf(Props[SauerbratenPinger], name = "pingerClearer")
  val persistence = context.actorSelection("persistence")
  //val persistence = context.actorOf(Props[PingPersistence], name = "persistence")
  def receive = {
    case ping @ Ping(who @ InetPair(host, port)) =>
      persistence ! ping
      context.parent ! ping
    case message: ReceivedMessage =>
      persistence ! message
      context.parent ! message
    case message: ReceivedBadMessage =>
      persistence ! message
      context.parent ! message
  }
}
