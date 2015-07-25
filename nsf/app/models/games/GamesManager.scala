package models.games

import javax.inject.{Inject, Singleton}

import akka.agent.Agent
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import models.SimpleEmbeddedDatabase
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
 * Created on 13/07/2015.
 */
@Singleton
class GamesManager @Inject()(simpleEmbeddedDatabase: SimpleEmbeddedDatabase)(implicit executionContext: ExecutionContext, applicationLifecycle: ApplicationLifecycle) {

  val duelsMap = simpleEmbeddedDatabase.get().openMap[String, String]("duels")

  val ctfMap = simpleEmbeddedDatabase.get().openMap[String, String]("ctf")
  import collection.JavaConverters._
  val gamesA = Agent(Games(
    duels = {
      duelsMap.asScala.toList
        .flatMap{case (k, v) =>
        Try(k -> SimpleCompletedDuel.fromPrettyJson(v)).toOption}.toMap
    },
  ctfs = {
    ctfMap.asScala.toList
      .flatMap{case (k, v) =>
      Try(k -> SimpleCompletedCTF.fromPrettyJson(v)).toOption}.toMap
  }
  ))

  def games: Games = gamesA.get()

  def addDuel(duel: SimpleCompletedDuel): Unit = {
    duelsMap.put(duel.simpleId, duel.toPrettyJson)
    simpleEmbeddedDatabase.commit()
    gamesA.alter(_.withDuel(duel))
  }

  def addCtf(ctf: SimpleCompletedCTF): Unit = {
    ctfMap.put(ctf.simpleId, ctf.toPrettyJson)
    simpleEmbeddedDatabase.commit()
    gamesA.alter(_.withCtf(ctf))
  }

}
