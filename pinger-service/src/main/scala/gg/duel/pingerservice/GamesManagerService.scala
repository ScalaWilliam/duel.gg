package gg.duel.pingerservice

import java.io.{File, FileInputStream, FileOutputStream}

import akka.agent.Agent
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class GamesManagerService (filePath: String)(implicit executionContext: ExecutionContext) {
  val gamesA = Agent(GamesContainer.empty)
  import concurrent.blocking
  val f = new File(filePath)
  val outputFileStream = new FileOutputStream(f, true)
  val dw = new GamesJournalWriter(outputFileStream)
  val gamesLoadedF = Future{blocking{
    val fis = new FileInputStream(f)
    try GamesContainer(
      games = GamesJournalReader.fromInputStream(fis).toMap
    ) finally fis.close()
  }}.flatMap(gamesContainer => gamesA.alter(_ ++ gamesContainer))

  def games: GamesContainer = gamesA.get()

  def addDuel(duel: SimpleCompletedDuel): Unit = {
    dw.write(duel.startTimeText, duel.toPlayJson)
    gamesA.alter(_.withGame(duel.startTimeText, Json.parse(duel.toJson)))
  }

  def stop(): Unit = {
    outputFileStream.close()
  }

}

