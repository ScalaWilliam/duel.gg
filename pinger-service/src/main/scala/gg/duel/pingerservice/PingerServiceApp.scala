package gg.duel.pingerservice

import akka.actor.ActorSystem

/**
  * Created by me on 09/07/2016.
  */
object PingerServiceApp extends App {
  implicit val actorSystem = ActorSystem("actors")
  val gms = new GamesManagerService("gemas.txt")
  val sm = new ServerManager()
  val ps = new PingerService(sm, gms)
  val js = new JournallingService()
}
